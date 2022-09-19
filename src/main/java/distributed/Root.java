package distributed;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.ClusterSetup;
import akka.japi.Pair;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import distributed.model.CoordinatorZone;
import distributed.model.Sensor;
import distributed.utils.CalculatorZone;

import java.util.*;

public class Root {

    private static final String CLUSTER_NAME = "PluviometerCluster";
    private static final String DEFAULT_HOSTNAME = "127.0.0.1";
    private static final int DEFAULT_ZONES_PORT = 2550;
    private static final int DEFAULT_SENSORS_PORT = 2660;
    private int zoneCounter;
    private City city;
    private static CalculatorZone calculatorZone;
    private static ActorSystem<Void> clusterRootNode;


    public Root(final City c) {
        this.city = c;
        this.calculatorZone = new CalculatorZone(this.city);
        this.zoneCounter = 0;
    }

    public static void startup(int port) {
        // Represents the seednodes's list of the cluster root
        List<String> clusterSeedNodes = new ArrayList<>();

        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();

        overrides.put("akka.log-level", "DEBUG"); // akka debug
        overrides.put("akka.actor.provider", "cluster");
        overrides.put("akka.discovery.method", "config");
        overrides.put("akka.remote.artery.enabled", "on");
        overrides.put("akka.remote.artery.transport", "tcp");
        overrides.put("akka.remote.artery.canonical.hostname", DEFAULT_HOSTNAME);
        overrides.put("akka.cluster.jmx.multi-mbeans-in-same-jvm", "on"); // used to boot multiple cluster nodes/jvm on the same machine
        overrides.put("akka.cluster.downing-provider-class", "akka.cluster.sbr.SplitBrainResolverProvider");

        /* Its first element must have the same cluster name, hostname and port of the cluster root (implemented below).
        In this way, the cluster root will be elected as the first leader node and so the other
        cluster nodes will be able to join it in the same cluster. */
        clusterSeedNodes.add("akka://" + CLUSTER_NAME + "@" + DEFAULT_HOSTNAME + ":" + port);

        // Create an Akka ActorSystem for each node in the cluster representing sensors or zone coordinators
        createClusterNodes(clusterSeedNodes, overrides, port);

        // Create the Akka cluster root node in order to join all others cluster nodes
        clusterRootNode = ActorSystem.create(Behaviors.ignore(), CLUSTER_NAME, setConfig(overrides, clusterSeedNodes, port));

        /**
         * Debug of cluster
         */
        Cluster cluster = Cluster.get(clusterRootNode);
        try {
            Thread.sleep(15000);
            cluster.state().getMembers().forEach(x -> System.out.println(x));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void createClusterNodes(final List<String> clusterSeedNodes, final Map<String, Object> overrides, final int clusterRootPort) {
        int zoneCounter = 0;

        for (Pair<Integer, Integer> node : calculatorZone.setZoneSensors()) {
            int zoneNumber = node.second();
            int sensorNumber = node.first();

            if (zoneNumber > zoneCounter) {
                System.out.println("ZoneCoordinator " + zoneNumber);
                ActorSystem.create(rootZoneBehavior(zoneNumber), CLUSTER_NAME,
                        setConfig(overrides, Arrays.asList("akka://" + CLUSTER_NAME + "@" + DEFAULT_HOSTNAME + ":" + clusterRootPort), DEFAULT_ZONES_PORT + zoneNumber));

                clusterSeedNodes.add("akka://" + CLUSTER_NAME + "@" + DEFAULT_HOSTNAME + ":" + (DEFAULT_ZONES_PORT + zoneNumber));
                zoneCounter = zoneNumber;
            }
            System.out.println("Sensor " + sensorNumber);
            // Create an actor that handles cluster domain events
            ActorSystem.create(rootSensorBehavior(sensorNumber, zoneNumber), CLUSTER_NAME,
                    setConfig(overrides, Arrays.asList("akka://" + CLUSTER_NAME + "@" + DEFAULT_HOSTNAME + ":" + clusterRootPort), DEFAULT_SENSORS_PORT + sensorNumber));

            clusterSeedNodes.add("akka://" + CLUSTER_NAME + "@" + DEFAULT_HOSTNAME + ":" + (DEFAULT_SENSORS_PORT + sensorNumber));
        }
    }

    public static Config setConfig(final Map<String, Object> overrides, final List<String> seednodes, final int port) {
        Map<String, Object> configs = new HashMap<>(overrides);
        configs.put("akka.remote.artery.canonical.port", port);
        configs.put("akka.cluster.seed-nodes", seednodes);
        System.out.println(configs);
        return ConfigFactory.parseMap(configs).withFallback(ConfigFactory.load());
    }

    public static Behavior<Void> rootZoneBehavior(final int zoneNumber) {
        return Behaviors.setup(context -> {
            context.spawn(CoordinatorZone.create(zoneNumber), "ZoneCoordinator" + zoneNumber);
            return Behaviors.same();
        });
    }

    public static Behavior<Void> rootSensorBehavior(final int sensorNumber, final int zoneNumber) {
        return Behaviors.setup(context -> {
            // Create an actor that handles cluster domain events
            context.spawn(Sensor.create(sensorNumber, zoneNumber), "Sensor" + sensorNumber);
            return Behaviors.same();
        });
    }
}
