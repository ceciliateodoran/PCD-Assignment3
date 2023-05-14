package distributed;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.Props;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.pubsub.Topic;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Join;
import akka.japi.Pair;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import distributed.messages.ValueMsg;
import distributed.model.*;
import distributed.utils.CalculatorZone;

import java.lang.reflect.Array;
import java.util.*;

public class Deployer {
    private static final String CLUSTER_NAME = "PluviometerCluster";
    private static final String DEFAULT_HOSTNAME = "127.0.0.1";
    private static final String PATH = "akka://" + CLUSTER_NAME + "@" + DEFAULT_HOSTNAME + ":";
    private static final int DEFAULT_ZONES_PORT = 2550;
    private static final int DEFAULT_SENSORS_PORT = 2660;
    private static final int DEFAULT_BARRACK_PORT = 2870;
    private static City city;
    private static CalculatorZone calculatorZone;
    private static ActorSystem<Void> clusterRootNode;
    private static ActorSystem<Barrack> barrackNode;
    private static ActorSystem<CoordinatorZone> coordinatorZoneNode;
    private static ActorSystem<Sensor> sensorNode;

    public Deployer(final City c) {
        city = c;
        calculatorZone = new CalculatorZone(city);
    }
    public static void startup(int clusterRootPort) {
        // Override the configuration of the port
        Map<String, Object> settings = new HashMap<>();

        settings.put("akka.log-level", "DEBUG"); // akka debug
        //settings.put("akka.stdout-loglevel", "OFF");
        //settings.put("akka.loglevel", "OFF");
        settings.put("akka.actor.provider", "cluster");
        settings.put("akka.discovery.method", "config");
        settings.put("akka.remote.artery.enabled", "on");
        settings.put("akka.remote.artery.transport", "tcp");
        settings.put("akka.remote.artery.canonical.hostname", DEFAULT_HOSTNAME);
        settings.put("akka.cluster.jmx.multi-mbeans-in-same-jvm", "on"); // used to boot multiple cluster nodes/jvm on the same machine
        settings.put("akka.cluster.downing-provider-class", "akka.cluster.sbr.SplitBrainResolverProvider");
        settings.put("akka.actor.serialization-bindings." + '"' + "distributed.messages.ValueMsg" + '"', "jackson-json"); // used to serialize messages

        // Create the Akka cluster root node in order to join all others cluster nodes
        clusterRootNode = ActorSystem.create(Behaviors.ignore(), CLUSTER_NAME, setConfig(settings, Arrays.asList(PATH + clusterRootPort), clusterRootPort));

        barrackNode = ActorSystem.create(Behaviors.empty(), CLUSTER_NAME,
                setConfig(settings, Arrays.asList(PATH + clusterRootPort), DEFAULT_BARRACK_PORT));
        coordinatorZoneNode = ActorSystem.create(Behaviors.empty(), CLUSTER_NAME,
                setConfig(settings, Arrays.asList(PATH + clusterRootPort), DEFAULT_ZONES_PORT));
        sensorNode = ActorSystem.create(Behaviors.empty(), CLUSTER_NAME,
                setConfig(settings, Arrays.asList(PATH + clusterRootPort), DEFAULT_SENSORS_PORT));

        // Create an Akka ActorSystem for each node in the cluster representing sensors or zone coordinators
        createClusterNodes(settings, clusterRootPort);

        /**
         * Debug of cluster
         */
        Cluster cluster = Cluster.get(clusterRootNode);
        try {
            Thread.sleep(5000);
            cluster.state().getMembers().forEach(x -> System.out.println("address: " + x.address()));
            // Thread.sleep(5000);
            // clusterRootNode.terminate(); //vedere la teoria sull'importanza dell'avere un leader del cluster + lab di akka spiegati da Aguzzi
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    private static void createClusterNodes(final Map<String, Object> overrides, final int clusterRootPort) {
        IdGenerator idGen = new IdGenerator();
        int sensorsCounter = 1;

        for (final CityZone zone : calculatorZone.setSensorsInZones()) {
            int zoneNumber = zone.getIndex();

            // creation of Barracks and its GUIs
            System.out.println("Barrack " + zoneNumber);
            barrackNode.systemActorOf(Barrack.create(zoneNumber), "barrack" + zoneNumber, Props.empty());

            System.out.println("ZoneCoordinator " + zoneNumber);
            coordinatorZoneNode.systemActorOf(CoordinatorZone.create(idGen.getZoneId(zoneNumber), zoneNumber, city.getSensors()), "zone" + zoneNumber, Props.empty());

            for (final Map.Entry<String, Pair<Integer, Integer>> zoneSensors : zone.getSensors().entrySet()) {
                System.out.println("Sensor " + sensorsCounter);
                // Create an actor that handles cluster domain events
                sensorNode.systemActorOf(Sensor.create(idGen.getSensorId(zoneNumber,sensorsCounter), zoneNumber, zoneSensors.getValue(),
                        city.getLimit()), "sensor" + sensorsCounter, Props.empty());
                sensorsCounter++;
            }
        }
    }

    private static Config setConfig(final Map<String, Object> settings, final List<String> seedNodes, final int port) {
        settings.put("akka.remote.artery.canonical.port", port);
        settings.put("akka.cluster.seed-nodes", seedNodes);
        System.out.println(settings);
        return ConfigFactory.parseMap(settings).withFallback(ConfigFactory.load());
    }
}
