package distributed;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Down;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import distributed.messages.ValueMsg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class GenericClusterCreationTest {
    private static final int CLUSTER_ROOT_PORT = 2440;
    private static final int NODE1_PORT = 6000;
    private static final String CLUSTER_NAME = "PluviometerCluster";
    private static final String DEFAULT_HOSTNAME = "127.0.0.1";
    private static final String NODES_HOSTNAME = "127.3.0.6";
    private static final String PATH = "akka://" + CLUSTER_NAME + "@" + DEFAULT_HOSTNAME + ":";

    /**
     * Checks whether a cluster is created following the specifications in a Config object.
     *
     * @throws InterruptedException if interrupted while sleeping
     */
    @Test
    public void testGenericClusterCreationViaConfig() throws InterruptedException {
        // setting the system configuration object
        Config config = setConfig(List.of(PATH + CLUSTER_ROOT_PORT), DEFAULT_HOSTNAME, CLUSTER_ROOT_PORT);
        // create a cluster using the specified configuration
        ActorSystem<ValueMsg> system = ActorSystem.create(Behaviors.empty(), CLUSTER_NAME, config);

        // getting the system cluster to check whether it is active
        Cluster cluster = Cluster.get(system);
        Thread.sleep(2000);
        Assertions.assertEquals(cluster.state().leader().get(), system.address());

        cluster.manager().tell(Down.apply(cluster.selfMember().address()));
        Thread.sleep(10000);
    }

    /**
     * Checks whether nodes belong to a cluster.
     *
     * @throws InterruptedException if interrupted while sleeping
     */
    @Test
    public void testGenericNodesBelongsToCluster() throws InterruptedException {
        // setting the system configuration object
        Config config = setConfig(List.of(PATH + CLUSTER_ROOT_PORT), DEFAULT_HOSTNAME, CLUSTER_ROOT_PORT);
        // setting the configuration object for the first cluster node
        Config nodeConfig1 = setConfig(List.of(PATH + CLUSTER_ROOT_PORT), NODES_HOSTNAME, NODE1_PORT);
        // setting the configuration object for the second cluster node
        Config nodeConfig2 = setConfig(List.of(PATH + CLUSTER_ROOT_PORT), NODES_HOSTNAME, NODE1_PORT + 1);

        // create a cluster using its specified configuration
        ActorSystem<ValueMsg> system = ActorSystem.create(Behaviors.empty(), CLUSTER_NAME, config);
        // create the first cluster node using its specified configuration
        ActorSystem<ValueMsg> node1 = ActorSystem.create(Behaviors.empty(), CLUSTER_NAME, nodeConfig1);
        // create the second cluster node without its specified configuration
        ActorSystem<ValueMsg> node2 = ActorSystem.create(Behaviors.empty(), CLUSTER_NAME);

        // getting the system cluster to check whether the two nodes belong to the system.
        Cluster cluster = Cluster.get(system);
        Thread.sleep(5000);
        Assertions.assertTrue(cluster.state()._3().contains(node1.address()));
        Assertions.assertFalse(cluster.state()._3().contains(node2.address())); // the second node does not belong to the cluster because it does not have a configuration

        // create the second cluster node using its specified configuration
        node2 = ActorSystem.create(Behaviors.empty(), CLUSTER_NAME, nodeConfig2);
        Thread.sleep(5000);
        Assertions.assertTrue(cluster.state()._3().contains(node2.address())); // now the second node belongs to the cluster because it has its configuration

        cluster.manager().tell(Down.apply(cluster.selfMember().address()));
        Thread.sleep(10000);
    }

    private static Map<String, Object> initBasicConfig() {
        final Map<String, Object> settings = new HashMap<>();
        settings.put("akka.log-level", "DEBUG"); // akka debug
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

}
