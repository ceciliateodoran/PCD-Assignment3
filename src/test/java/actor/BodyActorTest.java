package actor;

import actor.message.ControllerMsg;
import actor.message.test.DistributedTestResult;
import actor.message.test.SerialTestResult;
import actor.message.test.StartTest;
import akka.actor.typed.ActorSystem;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.Behavior;
import distributed.messages.ValueMsg;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BodyActorTest {
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testBodiesCalculations() throws InterruptedException {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 1000;
        int maxIter = 30;

        Behavior<ControllerMsg> controllerActor = ControllerActor.create(totBodies, maxIter, width, height, true);
        ActorSystem<ControllerMsg> system = ActorSystem.create(controllerActor, "ControllerActor");

        system.tell(new StartTest(probe.ref(), false));

        SerialTestResult serialResultMsg = probe.expectMessageClass(SerialTestResult.class, Duration.ofSeconds(15));
        DistributedTestResult distributedResultMsg = probe.expectMessageClass(DistributedTestResult.class, Duration.ofSeconds(15));

        Thread.sleep(2000);

        serialResultMsg.getBodies().sort((x, y) -> Math.min(x.getId(), y.getId()));
        distributedResultMsg.getBodies().sort((x, y) -> Math.min(x.getId(), y.getId()));

        for(int i = 0; i < totBodies; i++) {
            assertEquals(serialResultMsg.getBodies().get(i), distributedResultMsg.getBodies().get(i));
        }
    }

    @Test
    public void test1000iter100bodies() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 100;
        int maxIter = 1000;

        Behavior<ControllerMsg> controllerActor = ControllerActor.create(totBodies, maxIter, width, height, true);
        ActorSystem<ControllerMsg> system = ActorSystem.create(controllerActor, "ControllerActor");

        system.tell(new StartTest(probe.ref(), true));

        probe.expectMessageClass(DistributedTestResult.class, Duration.ofMillis(800));
    }

    @Test
    public void test1000iter1000bodies() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 1000;
        int maxIter = 1000;

        Behavior<ControllerMsg> controllerActor = ControllerActor.create(totBodies, maxIter, width, height, true);
        ActorSystem<ControllerMsg> system = ActorSystem.create(controllerActor, "ControllerActor");

        system.tell(new StartTest(probe.ref(), true));

        probe.expectMessageClass(DistributedTestResult.class, Duration.ofMillis(10000));
    }

    @Test
    public void test1000iter5000bodies() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 5000;
        int maxIter = 1000;

        Behavior<ControllerMsg> controllerActor = ControllerActor.create(totBodies, maxIter, width, height, true);
        ActorSystem<ControllerMsg> system = ActorSystem.create(controllerActor, "ControllerActor");

        system.tell(new StartTest(probe.ref(), true));

        probe.expectMessageClass(DistributedTestResult.class, Duration.ofMillis(100000));
    }

    @Test
    public void test10000iter100bodies() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 100;
        int maxIter = 10000;

        Behavior<ControllerMsg> controllerActor = ControllerActor.create(totBodies, maxIter, width, height, true);
        ActorSystem<ControllerMsg> system = ActorSystem.create(controllerActor, "ControllerActor");

        system.tell(new StartTest(probe.ref(), true));

        probe.expectMessageClass(DistributedTestResult.class, Duration.ofMillis(100000));
    }

    @Test
    public void test10000iter1000bodies() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 1000;
        int maxIter = 10000;

        Behavior<ControllerMsg> controllerActor = ControllerActor.create(totBodies, maxIter, width, height, true);
        ActorSystem<ControllerMsg> system = ActorSystem.create(controllerActor, "ControllerActor");

        system.tell(new StartTest(probe.ref(), true));

        probe.expectMessageClass(DistributedTestResult.class, Duration.ofMillis(100000));
    }

    @Test
    public void test10000iter5000bodies() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 5000;
        int maxIter = 10000;

        Behavior<ControllerMsg> controllerActor = ControllerActor.create(totBodies, maxIter, width, height, true);
        ActorSystem<ControllerMsg> system = ActorSystem.create(controllerActor, "ControllerActor");

        system.tell(new StartTest(probe.ref(), true));

        probe.expectMessageClass(DistributedTestResult.class, Duration.ofMillis(1000000));
    }

    @Test
    public void test50000iter100bodies() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 100;
        int maxIter = 50000;

        Behavior<ControllerMsg> controllerActor = ControllerActor.create(totBodies, maxIter, width, height, true);
        ActorSystem<ControllerMsg> system = ActorSystem.create(controllerActor, "ControllerActor");

        system.tell(new StartTest(probe.ref(), true));

        probe.expectMessageClass(DistributedTestResult.class, Duration.ofMillis(10000));
    }

    @Test
    public void test50000iter1000bodies() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 1000;
        int maxIter = 50000;

        Behavior<ControllerMsg> controllerActor = ControllerActor.create(totBodies, maxIter, width, height, true);
        ActorSystem<ControllerMsg> system = ActorSystem.create(controllerActor, "ControllerActor");

        system.tell(new StartTest(probe.ref(), true));

        probe.expectMessageClass(DistributedTestResult.class, Duration.ofMillis(1000000));
    }
    
    @Test
    public void test50000iter5000bodies() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 5000;
        int maxIter = 50000;

        Behavior<ControllerMsg> controllerActor = ControllerActor.create(totBodies, maxIter, width, height, true);
        ActorSystem<ControllerMsg> system = ActorSystem.create(controllerActor, "ControllerActor");

        system.tell(new StartTest(probe.ref(), true));

        probe.expectMessageClass(DistributedTestResult.class, Duration.ofMillis(90000000));
    }
}
