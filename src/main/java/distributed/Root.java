package distributed;

import akka.actor.ActorSystem;
import akka.actor.Address;
import akka.actor.AddressFromURIString;
import akka.actor.Props;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.receptionist.ServiceKey;
import akka.cluster.Cluster;
import akka.japi.Pair;
import akka.management.javadsl.AkkaManagement;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import distributed.messages.ValueMsg;
import distributed.model.ClusterListenerActor;
import distributed.model.CoordinatorZone;
import distributed.model.Sensor;
import distributed.utils.CalculatorZone;

import java.util.*;

public class Root {
    private static final int DEFAULT_ZONES_PORT = 2550;
    private static final int DEFAULT_SENSORS_PORT = 8080;
    private City city;
    private static CalculatorZone calculatorZone;
    private static List<CityZone> cityZone;
    private static ActorSystem actorSystem;


    public Root(final City c) {
        this.city = c;
        this.calculatorZone = new CalculatorZone(this.city);
        this.cityZone = new ArrayList<>();
    }
/*
    private static void startupClusterNodes(List<String> ports) {
        System.out.printf("Start cluster on port(s) %s%n", ports);

        ports.forEach(port -> {
            ActorSystem actorSystem = ActorSystem.create("cluster", setupClusterNodeConfig(port));

            AkkaManagement.get(actorSystem).start();

            actorSystem.actorOf(ClusterListenerActor.props(), "clusterListener");

            addCoordinatedShutdownTask(actorSystem, CoordinatedShutdown.PhaseClusterShutdown());

            actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());
        });
    }

    private static Config setupClusterNodeConfig(String port) {
        return ConfigFactory.parseString(
                        String.format("akka.remote.netty.tcp.port=%s%n", port) +
                                String.format("akka.remote.artery.canonical.port=%s%n", port))
                .withFallback(ConfigFactory.load());
    }

    private static void addCoordinatedShutdownTask(ActorSystem actorSystem, String coordindateShutdownPhase) {
        CoordinatedShutdown.get(actorSystem).addTask(
                coordindateShutdownPhase,
                coordindateShutdownPhase,
                () -> {
                    actorSystem.log().warning("Coordinated shutdown phase {}", coordindateShutdownPhase);
                    return CompletableFuture.completedFuture(Done.getInstance());
                });
    }*/

    public static void startup(int port) {
        final int[] zone = {0};
        String clusterName = "PluviometerCluster";
        String hostname = "127.0.0.1";
        List<Address> clusterSeedNodes = new ArrayList<>();

        // Override the configuration of the port
        Map<String, Object> overrides = new HashMap<>();
        overrides.put("akka.actor.provider", "cluster");
        overrides.put("akka.discovery.method", "config");
        overrides.put("akka.remote.artery.transport", "tcp");
        //overrides.put("akka.remote.artery.canonical.port", port);
        //overrides.put("akka.remote.artery.canonical.hostname", hostname);
        //clusterSeedNodes.add(AddressFromURIString.parse("akka://"+ clusterName + "@" + hostname + ":" + port));
        //clusterSeedNodes.addAll(setConfigs(overrides, clusterName, hostname));

        /*Config config = ConfigFactory.parseMap(overrides)
                .withFallback(ConfigFactory.load());*/

        // Create an Akka system for each node in the cluster representing sensors or zone coordinators
        calculatorZone.setZoneSensors().forEach(
                nodes -> {
                    int zoneNumber = nodes.second();
                    int sensorNumber = nodes.first();

                    if (zoneNumber > zone[0]) {
                        System.out.println("ZoneCoordinator " + zoneNumber);
                        actorSystem = ActorSystem.create(clusterName,
                                setConfig(overrides, clusterName, "ZoneCoordinator" + zoneNumber, DEFAULT_ZONES_PORT));
                        AkkaManagement.get(actorSystem).start();
                        actorSystem.actorOf(CoordinatorZone.props(), "ZoneCoordinator" + zoneNumber);
                        zone[0] = zoneNumber;
                    }
                    System.out.println("Sensor " + sensorNumber);
                    // Create an actor that handles cluster domain events
                    actorSystem = ActorSystem.create(clusterName,
                            setConfig(overrides, clusterName, "Sensor" + sensorNumber, DEFAULT_SENSORS_PORT));
                    AkkaManagement.get(actorSystem).start();
                    actorSystem.actorOf(Sensor.props(), "Sensor" + sensorNumber);

                    //addCoordinatedShutdownTask(actorSystem, CoordinatedShutdown.PhaseClusterShutdown());

                    //actorSystem.log().info("Akka node {}", actorSystem.provider().getDefaultAddress());
                }
        );

        //Cluster cluster = Cluster.get(actorSystem);
        //cluster.manager().tell(new JoinSeedNodes(clusterSeedNodes));
        //System.out.println(system.printTree());
    }

    public static Config setConfig(final Map<String, Object> c, final String clusterName, final String hostname, final int port) {
        Map<String, Object> configs = new HashMap<>(c);
        configs.put("akka.cluster.seed-nodes", Arrays.asList("akka://"+ clusterName + "@" + hostname + ":" + port));
        return ConfigFactory.parseMap(configs).withFallback(ConfigFactory.load());
    }

    /*public static Behavior<Void> rootBehavior() {
        return Behaviors.setup(context -> {
            List<Pair<Integer, Integer>> zones = calculatorZone.setZoneSensors();
            ServiceKey<ValueMsg> key = null;
            ActorRef<ValueMsg> coordinator = null;

            int zone = 0;
            for (int i = 0; i < zones.size(); i++) {
                int zoneNumber = zones.get(i).second();
                int sensorNumber = zones.get(i).first();
                if (zoneNumber > zone) {
                    key = ServiceKey.create(ValueMsg.class, "SensoreZona" + zoneNumber);
                    coordinator = context.spawn(CoordinatorZone.create(zoneNumber, key), "ZoneCoordinator" + zoneNumber);
                    zone = zoneNumber;
                }
                // Create an actor that handles cluster domain events
                context.spawn(Sensor.create(coordinator, sensorNumber, zone, key), "Sensor" + sensorNumber);
            }

            return Behaviors.empty();
        });
    }*/

    /*static List<Address> setConfigs(final Map<String, Object> overrides, final String clusterName, final String hostname) {
        int zone = 0;
        String clusterPath = "akka://" + clusterName + "@" + hostname + ":";

        List<Pair<String, String>> zp = new ArrayList<>();
        List<Pair<String, String>> sp = new ArrayList<>();
        List<Address> seedNodes = new ArrayList<>();

        List<Pair<Integer, Integer>> zones = calculatorZone.setZoneSensors();

        for (int i = 0; i < calculatorZone.setZoneSensors().size(); i++) {
            int zoneNumber = zones.get(i).second();
            int sensorNumber = zones.get(i).first();
            String sensorPort = Integer.valueOf(DEFAULT_SENSORS_ZONE + sensorNumber).toString();
            if (zoneNumber > zone) {
                String zonePort = Integer.valueOf(DEFAULT_ZONES_PORT + i).toString();
                zp.add(new Pair<>("host: " + '"'+"ZoneCoordinator" + zoneNumber+'"', "port: " + zonePort));
                seedNodes.add(AddressFromURIString.parse(clusterPath + zonePort));
                zone = zoneNumber;
            }
            sp.add(new Pair<>("host: " + '"'+"Sensor" + sensorNumber+'"', "port: " + sensorPort));
            seedNodes.add(AddressFromURIString.parse(clusterPath + sensorPort));
        }

        overrides.put("akka.discovery.config.services.zones.endpoints",
                "[" + zp.stream().map(x -> "{ " + x.first() + " " + x.second() + " }").reduce((x, y) -> x + ", " + y).get() + "]");
        overrides.put("akka.discovery.config.services.sensors.endpoints",
                "[" + sp.stream().map(x -> "{ " + x.first() + " " + x.second() + " }").reduce((x, y) -> x + ", " + y).get() + "]");

        return seedNodes;
    }*/
}
