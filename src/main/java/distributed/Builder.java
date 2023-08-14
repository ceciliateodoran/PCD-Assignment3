package distributed;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.*;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import distributed.messages.spawn.SpawnBarrackActor;
import distributed.messages.spawn.SpawnGuiActor;
import distributed.messages.spawn.SpawnSensorActor;
import distributed.messages.spawn.SpawnZoneActor;
import distributed.messages.ValueMsg;
import distributed.model.*;
import distributed.model.utility.IdGenerator;
import distributed.utils.CalculatorZone;
import distributed.utils.ClusterStructure;
import distributed.utils.Pair;
import distributed.view.View;

import java.util.*;

/**
 * Consists of the deployer of all the distributed system components
 */
public class Builder {
    private static final String CLUSTER_NAME = "PluviometerCluster";
    private static final String DEFAULT_HOSTNAME = "127.0.0.1";
    private static final String PATH = "akka://" + CLUSTER_NAME + "@" + DEFAULT_HOSTNAME + ":";
    private static final int DEFAULT_GUI_PORT = 2700;
    private static City city;
    private static CalculatorZone calculatorZone;
    private static ActorSystem<ValueMsg> clusterRootNode;
    private static ActorSystem<ValueMsg> barrackZoneNode;
    private static ActorSystem<ValueMsg> sensorNode;
    private static ActorSystem<ValueMsg> guiNode;
    private static ClusterStructure clusterStructure;

    /**
     * Construct a new instance of the Builder
     *
     * @param c The specific city to consider for the distributed system building
     */
    public Builder(final City c) {
        city = c;
        clusterStructure = new ClusterStructure();
        calculatorZone = new CalculatorZone(city, clusterStructure);
        clusterStructure.setClusterName(CLUSTER_NAME);
    }

    /**
     * Starts the distributed system building
     *
     * @param clusterRootPort The specific port number of the cluster root node
     * @return The distributed system built
     */
    public static ActorSystem<ValueMsg> startup(int clusterRootPort) {
        // Create the Akka cluster root node in order to join all others cluster nodes
        clusterRootNode = ActorSystem.create(Behaviors.empty(), CLUSTER_NAME, setConfig(List.of(PATH + clusterRootPort), DEFAULT_HOSTNAME, clusterRootPort));

        // Create an Akka ActorSystem for each node in the cluster representing sensors or zone coordinators
        createClusterNodes(clusterRootPort);

        return clusterRootNode;
    }

    private static void createClusterNodes(final int clusterRootPort) {
        IdGenerator idGen = new IdGenerator();
        int sensorsCounter = 0;

        for (final CityZone zone : calculatorZone.setSensorsInZones()) {
            int zoneNumber = zone.getIndex();
            System.out.println("Zone number: " + zoneNumber);

            for (final Map.Entry<String, Pair<Integer, Integer>> zoneSensors : zone.getSensors().entrySet()) {
                System.out.println("Sensor " + sensorsCounter);
                // Creation of the Actor System and the Actor for each sensor in the zone
                sensorNode = ActorSystem.create(Behaviors.supervise(Deployer.create()).onFailure(SupervisorStrategy.restart()), CLUSTER_NAME, setConfig(List.of(PATH + clusterRootPort),
                        clusterStructure.getPhysicalHostSensors().get(new Pair<>(sensorsCounter, zoneNumber)).first(),
                        clusterStructure.getPhysicalHostSensors().get(new Pair<>(sensorsCounter, zoneNumber)).second()));
                sensorNode.tell(new SpawnSensorActor(idGen.getSensorId(zoneNumber, sensorsCounter), zoneNumber, zoneSensors.getValue(), city.getLimit(), sensorsCounter));
                sensorsCounter++;
            }
            sensorsCounter = 0;

            /**
             * Creation of the Guis in the distributed system
             *
             * N.B.
             * Uncomment the following lines in case you don't use this program locally.
             * In that case, you should not use the "addGuiActor" method anywhere in the project.
             */
            /*guiNode = ActorSystem.create(Behaviors.supervise(Deployer.create()).onFailure(SupervisorStrategy.restart()), CLUSTER_NAME, setConfig(List.of(PATH + clusterRootPort),
                    clusterStructure.getPhysicalGuiSystems().get(zoneNumber).first(), clusterStructure.getPhysicalGuiSystems().get(zoneNumber).second()));
            guiNode.tell(new SpawnGuiActor(zoneNumber, getCityDimensions(), calculatorZone.setSensorsInZones()));*/

            // Creation of the Barrack and Zone Actor System
            System.out.println("Barrack " + zoneNumber);
            barrackZoneNode = ActorSystem.create(Behaviors.supervise(Deployer.create()).onFailure(SupervisorStrategy.restart()), CLUSTER_NAME, setClusterConfig(List.of(PATH + clusterRootPort),
                    clusterStructure.getPhysicalHostBarracksZones().get(zoneNumber).first(), clusterStructure.getPhysicalHostBarracksZones().get(zoneNumber).second()));


            // Update the cluster structure with the new zone
            clusterStructure.addZoneSystem(zoneNumber, new Pair<>(clusterStructure.getPhysicalHostBarracksZones().get(zoneNumber).first(), barrackZoneNode));

            // Creation of the Barrack Actor
            barrackZoneNode.tell(new SpawnBarrackActor(zoneNumber));

            System.out.println("ZoneCoordinator " + zoneNumber);
            // Creation of the Zone Actor
            barrackZoneNode.tell(new SpawnZoneActor(idGen.getZoneId(zoneNumber), zoneNumber, city.getSensors()));


        }
    }

    private static Map<String, Object> initBasicConfig() {
        final Map<String, Object> settings = new HashMap<>();
        settings.put("akka.log-level", "DEBUG"); // akka debug
        //settings.put("akka.stdout-loglevel", "OFF");
        //settings.put("akka.loglevel", "OFF");
        settings.put("akka.actor.provider", "cluster");
        settings.put("akka.discovery.method", "config");
        settings.put("akka.remote.artery.enabled", "on");
        settings.put("akka.remote.artery.transport", "tcp");
        settings.put("akka.cluster.jmx.multi-mbeans-in-same-jvm", "on"); // used to boot multiple cluster nodes/jvm on the same machine

        settings.put("akka.cluster.downing-provider-class", "akka.cluster.sbr.SplitBrainResolverProvider");

        settings.put("akka.actor.serializers.jackson-json", "akka.serialization.jackson.JacksonJsonSerializer");
        settings.put("akka.actor.serialization-bindings." + '"' + "java.util.List" + '"', "jackson-json");
        settings.put("akka.actor.serialization-bindings." + '"' + "java.util.ArrayList" + '"', "jackson-json");
        settings.put("akka.actor.serialization-bindings." + '"' + "distributed.utils.Pair" + '"', "jackson-json");
        settings.put("akka.actor.serialization-bindings." + '"' + "distributed.model.utility.SensorSnapshot" + '"', "jackson-json");
        settings.put("akka.actor.serialization-bindings." + '"' + "distributed.messages.ValueMsg" + '"', "jackson-json"); // used to serialize messages
        return settings;
    }

    private static Config setConfig(final List<String> seedNodes, final String hostname, final int port) {
        final Map<String, Object> settings = new HashMap<>(initBasicConfig());
        settings.put("akka.remote.artery.canonical.hostname", hostname);
        settings.put("akka.remote.artery.canonical.port", port);
        settings.put("akka.cluster.seed-nodes", seedNodes);
        return ConfigFactory.parseMap(settings).withFallback(ConfigFactory.load());
    }

    private static Config setClusterConfig(final List<String> seedNodes, final String hostname, final int port) {
        final Map<String, Object> settings = new HashMap<>(initBasicConfig());

        /* Cluster singleton configuration */
        settings.put("akka.cluster.roles", List.of("Controller", "Storing"));
        settings.put("akka.cluster.singleton.singleton-name", "Supervisor");
        settings.put("akka.cluster.singleton.role", "Controller");

        settings.put("akka.cluster.singleton.hand-over-retry-interval",  "1s");
        settings.put("akka.cluster.singleton.min-number-of-hand-over-retries", 15);
        settings.put("akka.cluster.singleton.use-lease", "");
        settings.put("akka.cluster.singleton.lease-retry-interval", "5s");

        settings.put("akka.cluster.singleton-proxy.singleton-name", "Supervisor");
        // The role of the cluster nodes where the singleton can be deployed
        settings.put("akka.cluster.singleton-proxy.role", "Controller");
        settings.put("akka.cluster.singleton-proxy.singleton-identification-interval", "1s");
        settings.put("akka.cluster.singleton-proxy.buffer-size", 1000);

        settings.put("akka.remote.artery.canonical.hostname", hostname);
        settings.put("akka.remote.artery.canonical.port", port);
        settings.put("akka.cluster.seed-nodes", seedNodes);
        return ConfigFactory.parseMap(settings).withFallback(ConfigFactory.load());
    }

    private static Map<String, Integer> getCityDimensions() {
        return Map.of("rows", city.getGridRows(), "columns", city.getGridColumns(), "width", city.getWidth(), "height", city.getHeight());
    }

    public ClusterStructure getClusterStructure() {
        return clusterStructure;
    }

    /**
     * It allows to add gui actor nodes to the distributed system created
     *
     * @param clusterRootPort The specific port number of the cluster root node
     * @param zone The number of the zone to which the gui belongs
     * @param cityDimensions The dimensions of the specified city
     */
    public void addGuiActor(final int clusterRootPort, final int zone, final Map<String, Integer> cityDimensions) {
        guiNode = ActorSystem.create(Behaviors.supervise(Deployer.create()).onFailure(SupervisorStrategy.restart()),
                CLUSTER_NAME, setConfig(List.of(PATH + clusterRootPort), "127.0.1.2", DEFAULT_GUI_PORT));
        System.out.println("Gui " + zone);
        guiNode.tell(new SpawnGuiActor(zone, cityDimensions, calculatorZone.setSensorsInZones()));
    }
}