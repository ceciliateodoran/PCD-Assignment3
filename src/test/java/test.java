import akka.actor.ActorPath;
import akka.actor.Address;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.pubsub.Topic;
import akka.japi.Pair;
import distributed.messages.ValueMsg;
import distributed.model.CoordinatorZone;
import distributed.model.Sensor;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;

public class test {
    @ClassRule public static final TestKitJunitResource testKit = new TestKitJunitResource();


    @Test
    public void testSomething() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();
        ActorPath.fromString(probe.getRef().path().toStringWithoutAddress())
        System.out.println(probe.getRef().path().toStringWithoutAddress());
        probe.tell(new ValueMsg());
        ActorRef<Topic.Command<ValueMsg>> zoneTopic = testKit.spawn(Topic.create(ValueMsg.class, "zone-" + 1 + "-channel"), "zone-" + 1 + "-topic");
        ActorRef<ValueMsg> coordinatorZone = testKit.spawn(CoordinatorZone.create("coordinator1", probe.getRef().path().name(),1, zoneTopic,2), "ping");
        for(int i = 0; i < 3; i++){
            testKit.spawn(Sensor.create("sensor"+i, 1, coordinatorZone.path().name(), zoneTopic, new Pair<Integer, Integer>(0,0)));
        }
        probe.expectMessage(new ValueMsg());
    }
}