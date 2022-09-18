package distributed;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.ClusterSetup;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import distributed.model.CoordinatorZone;
import distributed.model.Sensor;
import distributed.utils.CalculatorZone;

import java.util.*;

public class Root {
    private static final int DEFAULT_ZONES_PORT = 2550;
    private static final int DEFAULT_SENSORS_PORT = 2660;
    private City city;
    private static CalculatorZone calculatorZone;
    private static ActorSystem<Void> actorSystem;
    private static ActorSystem<Void> rootActorSystem;


    public Root(final City c) {
        this.city = c;
        this.calculatorZone = new CalculatorZone(this.city);
    }

    public static void startup(int port) {
        final int[] zone = {0};

        String clusterName = "PluviometerCluster";
        String hostname = "127.0.0.1";
        List<String> clusterSeedNodes = new ArrayList<>();

        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();

        overrides.put("akka.actor.provider", "cluster");
        overrides.put("akka.discovery.method", "config");
        overrides.put("akka.remote.artery.enabled", "on");
        overrides.put("akka.remote.artery.transport", "tcp");
        overrides.put("akka.remote.artery.canonical.hostname", hostname);
        overrides.put("akka.cluster.jmx.multi-mbeans-in-same-jvm", "on"); // serve per far avviare più nodi/jvm del cluster su una stessa macchina

        clusterSeedNodes.add("akka://" + clusterName + "@" + hostname + ":" + port);

        // Create an Akka system for each node in the cluster representing sensors or zone coordinators
        calculatorZone.setZoneSensors().forEach(
                nodes -> {
                    int zoneNumber = nodes.second();
                    int sensorNumber = nodes.first();

                    if (zoneNumber > zone[0]) {
                        System.out.println("ZoneCoordinator " + zoneNumber);
                        actorSystem = ActorSystem.create(rootZoneBehavior(zoneNumber), clusterName,
                                setConfig(overrides,
                                        Arrays.asList("akka://" + clusterName + "@" + hostname + ":" + port),
                                        DEFAULT_ZONES_PORT + zoneNumber));

                        clusterSeedNodes.add("akka://" + clusterName + "@" + hostname + ":" + (DEFAULT_ZONES_PORT + zoneNumber));
                        zone[0] = zoneNumber;
                    }
                    System.out.println("Sensor " + sensorNumber);
                    // Create an actor that handles cluster domain events
                    actorSystem = ActorSystem.create(rootSensorBehavior(sensorNumber, zoneNumber), clusterName,
                            setConfig(overrides,
                                    Arrays.asList("akka://" + clusterName + "@" + hostname + ":" + port),
                                    DEFAULT_SENSORS_PORT + sensorNumber));

                    clusterSeedNodes.add("akka://" + clusterName + "@" + hostname + ":" + (DEFAULT_SENSORS_PORT + sensorNumber));
                }
        );

        rootActorSystem = ActorSystem.create(Behaviors.ignore(), clusterName, setConfig(overrides, clusterSeedNodes, port));
        
        /**
         * Debug of cluster
         */
        Cluster cluster = Cluster.get(rootActorSystem);
        try {
            Thread.sleep(15000);
            cluster.state().getMembers().forEach(x -> System.out.println(x));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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
            return Behaviors.empty();
        });
    }

    public static Behavior<Void> rootSensorBehavior(final int sensorNumber, final int zoneNumber) {
        return Behaviors.setup(context -> {
            // Create an actor that handles cluster domain events
            context.spawn(Sensor.create(sensorNumber, zoneNumber), "Sensor" + sensorNumber);
            return Behaviors.empty();
        });
    }
}
