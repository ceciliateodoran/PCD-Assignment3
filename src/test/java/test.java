import akka.actor.ActorPath;
import akka.actor.Address;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.pubsub.Topic;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.japi.Pair;
import distributed.messages.ValueMsg;
import distributed.messages.ZoneStatus;
import distributed.model.CoordinatorZone;
import distributed.model.IdGenerator;
import distributed.model.Sensor;
import org.junit.ClassRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class test {
    @ClassRule public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testSomething() throws InterruptedException {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();
        Thread.sleep(3000);
        testKit.system().receptionist().tell(Receptionist.register(ServiceKey.create(ValueMsg.class, "barracks"), probe.ref()));
        Thread.sleep(3000);
        List<Sensor> sensors = new ArrayList<>();
        IdGenerator idGen = new IdGenerator();
        for (int i = 0; i < 3; i++){
            testKit.spawn(Sensor.create(idGen.getSensorId(0, i), 0, new Pair<>(0,0)));
            Thread.sleep(3000);
        }
        testKit.spawn(CoordinatorZone.create(idGen.getZoneId(0), 0, 3));
        Thread.sleep(6000);
        probe.expectMessageClass(ZoneStatus.class);
    }
}