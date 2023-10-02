import akka.actor.typed.ActorSystem;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.Down;
import distributed.Builder;
import distributed.City;
import distributed.messages.ValueMsg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SystemClusterCreationTest {
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
     * Checks whether the system cluster is created following the specifications in a Config object.
     *
     * @throws InterruptedException if interrupted while sleeping
     */
    @Test
    public void testSystemClusterCreationViaConfig() throws InterruptedException {
        ActorSystem<ValueMsg> rootClusterSystemNode = Builder.startup(CLUSTER_ROOT_PORT);

        Cluster cluster = Cluster.get(rootClusterSystemNode);

        Thread.sleep(10000);

        Assertions.assertEquals(cluster.state().leader().get(), rootClusterSystemNode.address());

        cluster.state().getMembers().forEach(x -> cluster.manager().tell(Down.apply(x.address())));
        cluster.manager().tell(Down.apply(cluster.selfMember().address()));
        Thread.sleep(10000);
    }

    /**
     * Checks whether the nodes belong to the system cluster.
     *
     * @throws InterruptedException if interrupted while sleeping
     */
    @Test
    public void testControllerNodesBelongsToSystemCluster() throws InterruptedException {
        ActorSystem<ValueMsg> rootClusterSystemNode = Builder.startup(CLUSTER_ROOT_PORT);

        Cluster cluster = Cluster.get(rootClusterSystemNode);

        Thread.sleep(10000);

        Assertions.assertEquals(cluster.state().members().filter(x -> x.getRoles().contains("Controller")).size(),
                builder.getClusterStructure().getZoneSystems().size());

        for (int i = 0; i < builder.getClusterStructure().getZoneSystems().size(); i++) {
            Assertions.assertTrue(cluster.state()._3().contains(builder.getClusterStructure().getZoneActorSystem(i).address()));
        }

        cluster.state().getMembers().forEach(x -> cluster.manager().tell(Down.apply(x.address())));
        cluster.manager().tell(Down.apply(cluster.selfMember().address()));
        Thread.sleep(10000);
    }

}
