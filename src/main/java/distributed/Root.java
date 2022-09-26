package distributed;

import actor.ViewActor;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.internal.receptionist.ReceptionistMessages;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.ClusterEvent;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Subscribe;
import akka.japi.Pair;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import distributed.messages.DetectedValueMsg;
import distributed.messages.ValueMsg;
import distributed.model.Barrack;
import distributed.model.CoordinatorZone;
import distributed.model.Sensor;
import distributed.utils.CalculatorZone;
import scala.Int;

import java.util.*;
import java.util.stream.Collectors;

public class Root {
    private static final String CLUSTER_NAME = "PluviometerCluster";
    private static final String DEFAULT_HOSTNAME = "127.0.0.1";
    private static final String PATH = "akka://" + CLUSTER_NAME + "@" + DEFAULT_HOSTNAME + ":";
    private static final String DEFAULT_GUARD_ACTOR = "/user/"; // guardian actor for all user-created top-level actors
    private static final int DEFAULT_ZONES_PORT = 2550;
    private static final int DEFAULT_SENSORS_PORT = 2660;
    private static final int DEFAULT_BARRACK_PORT = 2870;
    private City city;
    private static CalculatorZone calculatorZone;
    private static ActorSystem<Void> clusterRootNode;

    public Root(final City c) {
        this.city = c;
        this.calculatorZone = new CalculatorZone(this.city);
    }

    public static void startup(int clusterRootPort) {
        // Represents the seednodes's list of the cluster root
        List<String> clusterSeedNodes = new ArrayList<>();

        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();

        overrides.put("akka.log-level", "DEBUG"); // akka debug
        overrides.put("akka.stdout-loglevel", "OFF");
        overrides.put("akka.loglevel", "OFF");
        overrides.put("akka.actor.provider", "cluster");
        overrides.put("akka.discovery.method", "config");
        overrides.put("akka.remote.artery.enabled", "on");
        overrides.put("akka.remote.artery.transport", "tcp");
        overrides.put("akka.remote.artery.canonical.hostname", DEFAULT_HOSTNAME);
        overrides.put("akka.cluster.jmx.multi-mbeans-in-same-jvm", "on"); // used to boot multiple cluster nodes/jvm on the same machine
        overrides.put("akka.cluster.downing-provider-class", "akka.cluster.sbr.SplitBrainResolverProvider");
        overrides.put("akka.actor.serialization-bindings." + '"' + "distributed.messages.ValueMsg" + '"', "jackson-json"); // used to serialize messages

        /* Its first element must have the same cluster name, hostname and port of the cluster root (implemented below).
        In this way, the cluster root will be elected as the first leader node and so the other
        cluster nodes will be able to join it in the same cluster. */
        clusterSeedNodes.add(PATH + clusterRootPort);

        // Create an Akka ActorSystem for each node in the cluster representing sensors or zone coordinators
        createClusterNodes(clusterSeedNodes, overrides, clusterRootPort);

        // Create the Akka cluster root node in order to join all others cluster nodes
        clusterRootNode = ActorSystem.create(Behaviors.ignore(), CLUSTER_NAME, setConfig(overrides, clusterSeedNodes, clusterRootPort));

        /**
         * Debug of cluster
         */
        Cluster cluster = Cluster.get(clusterRootNode);
        try {
            Thread.sleep(15000);
            cluster.state().getMembers().forEach(x -> System.out.println(x.address()));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createClusterNodes(final List<String> clusterSeedNodes, final Map<String, Object> overrides, final int clusterRootPort) {
        int sensorsCounter = 0;
        String coordinatorRemotePath, barrackRemotePath;

        for (final CityZone zone : calculatorZone.setSensorsInZones()) {
            int zoneNumber = zone.getIndex();

            // creation of Barracks and its GUIs
            barrackRemotePath = PATH + (DEFAULT_BARRACK_PORT + zoneNumber) + DEFAULT_GUARD_ACTOR + "Barrack" + zoneNumber;
            ActorSystem.create(rootBarrackBehaviour(zoneNumber), CLUSTER_NAME,
                    setConfig(overrides, Arrays.asList(PATH + clusterRootPort), DEFAULT_BARRACK_PORT + zoneNumber));

            System.out.println("ZoneCoordinator " + zoneNumber);
            coordinatorRemotePath = PATH + (DEFAULT_ZONES_PORT + zoneNumber) + DEFAULT_GUARD_ACTOR;
            ActorSystem.create(rootZoneBehavior(zone.getIdZone(), zoneNumber, barrackRemotePath), CLUSTER_NAME,
                    setConfig(overrides, Arrays.asList(PATH + clusterRootPort), DEFAULT_ZONES_PORT + zoneNumber));
            clusterSeedNodes.add(PATH + (DEFAULT_ZONES_PORT + zoneNumber));

            for (final Map.Entry<String, Pair<Integer, Integer>> zoneSensors : zone.getSensors().entrySet()) {
                sensorsCounter++;
                System.out.println("Sensor " + sensorsCounter);
                // Create an actor that handles cluster domain events
                ActorSystem.create(rootSensorBehavior(zoneSensors.getKey(), zoneNumber,
                                coordinatorRemotePath + zone.getIdZone(), zoneSensors.getValue()),
                        CLUSTER_NAME,
                        setConfig(overrides, Arrays.asList(PATH + clusterRootPort), DEFAULT_SENSORS_PORT + sensorsCounter));

                clusterSeedNodes.add(PATH + (DEFAULT_SENSORS_PORT + sensorsCounter));
            }
        }
    }

    private static Config setConfig(final Map<String, Object> overrides, final List<String> seedNodes, final int port) {
        Map<String, Object> configs = new HashMap<>(overrides);
        configs.put("akka.remote.artery.canonical.port", port);
        configs.put("akka.cluster.seed-nodes", seedNodes);
        System.out.println(configs);
        return ConfigFactory.parseMap(configs).withFallback(ConfigFactory.load());
    }

    private static Behavior<CoordinatorZone> rootZoneBehavior(final String zoneID, final int zoneNumber, final String barrackPath) {
        return Behaviors.setup(context -> {
            context.spawn(CoordinatorZone.create(zoneID, barrackPath, zoneNumber), zoneID);
            return Behaviors.same();
        });
    }

    private static Behavior<Sensor> rootSensorBehavior(final String sensorID, final int zoneNumber, final String sensorCoordPath, final Pair<Integer, Integer> spaceCoords) {
        return Behaviors.setup(context -> {
            context.spawn(Sensor.create(sensorID, zoneNumber, sensorCoordPath, spaceCoords), sensorID);
            return Behaviors.same();
        });
    }

    private static Behavior<Barrack> rootBarrackBehaviour(final int zoneNumber) {
        return Behaviors.setup(context -> {
            context.spawn(Barrack.create(zoneNumber), "Barrack" + zoneNumber); // da cambiare la create di Barrack
            //context.spawn(ViewActor.create(context.getSelf(), 0, 0), "ClientView"); // da cambiare la create di ViewActor
            return Behaviors.same();
        });
    }
}
