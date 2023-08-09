import akka.actor.typed.ActorSystem;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Down;
import distributed.Builder;
import distributed.City;
import distributed.messages.ValueMsg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SystemClusterSingletonTest {
    private static final int CLUSTER_ROOT_PORT = 2440;
    private static City city;
    private static Builder builder;

    /**
     * Used to initialize the basic application variables, before each test
     */
    @BeforeEach
    public void initSystem() {
        city = new City(400, 200, 1, 1, 2, 100);
        builder = new Builder(city);
    }

    /**
     * Checks whether the Supervisor Cluster Singleton is created.
     *
     * @throws InterruptedException if interrupted while sleeping
     */
    @Test
    public void testSupervisorClusterSingleton() throws InterruptedException {
        ActorSystem<ValueMsg> rootClusterSystemNode = Builder.startup(CLUSTER_ROOT_PORT);

        Cluster cluster = Cluster.get(rootClusterSystemNode);

        Thread.sleep(10000);

        Assertions.assertEquals(cluster.state().members().filter(x -> x.getRoles().contains("Controller")).size(),
                builder.getClusterStructure().getZoneSystems().size());

        for (int i = 0; i < builder.getClusterStructure().getZoneSystems().size(); i++) {
            ActorSystem<ValueMsg> controllerSystem = builder.getClusterStructure().getZoneActorSystem(i);

            Assertions.assertTrue(controllerSystem.printTree().contains("singletonManagerSupervisor"));
            Assertions.assertTrue(controllerSystem.printTree().contains("singletonProxySupervisor"));
        }

        cluster.state().getMembers().forEach(x -> cluster.manager().tell(Down.apply(x.address())));
        cluster.manager().tell(Down.apply(cluster.selfMember().address()));
        Thread.sleep(10000);
    }

    /**
     * Checks whether the Store Cluster Singleton is created.
     *
     * @throws InterruptedException if interrupted while sleeping
     */
    @Test
    public void testStoreClusterSingleton() throws InterruptedException {
        ActorSystem<ValueMsg> rootClusterSystemNode = Builder.startup(CLUSTER_ROOT_PORT);

        Cluster cluster = Cluster.get(rootClusterSystemNode);

        Thread.sleep(10000);

        Assertions.assertEquals(cluster.state().members().filter(x -> x.getRoles().contains("Controller")).size(),
                builder.getClusterStructure().getZoneSystems().size());

        for (int i = 0; i < builder.getClusterStructure().getZoneSystems().size(); i++) {
            ActorSystem<ValueMsg> controllerSystem = builder.getClusterStructure().getZoneActorSystem(i);

            Assertions.assertTrue(controllerSystem.printTree().contains("singletonManagerStore"));
            Assertions.assertTrue(controllerSystem.printTree().contains("singletonProxyStore"));
        }

        cluster.state().getMembers().forEach(x -> cluster.manager().tell(Down.apply(x.address())));
        cluster.manager().tell(Down.apply(cluster.selfMember().address()));
        Thread.sleep(10000);
    }
}

