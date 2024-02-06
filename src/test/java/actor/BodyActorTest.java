package actor;

import actor.message.ControllerMsg;
import actor.message.test.DistributedTestResult;
import actor.message.test.SerialTestResult;
import actor.message.test.StartTest;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.Behavior;
import akka.actor.typed.MailboxSelector;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.ConfigFactory;
import distributed.messages.ValueMsg;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BodyActorTest {
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testBodiesCalculations() throws InterruptedException {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 100;
        int maxIter = 1000;

        ActorSystem<ControllerMsg> system = ActorSystem.create(
                Behaviors.setup(
                        (ctx) -> {
                            ctx.spawn(ControllerActor.create(totBodies, maxIter, width, height, true),
                                    "controllerActor",
                                    MailboxSelector.fromConfig("my-app.priority-mailbox"));
                            return Behaviors.same();
                        }
                ), "system", ConfigFactory.load());
;
        system.tell(new StartTest(probe.ref(), false));

        SerialTestResult serialResultMsg = probe.expectMessageClass(SerialTestResult.class, Duration.ofSeconds(25));
        DistributedTestResult distributedResultMsg = probe.expectMessageClass(DistributedTestResult.class, Duration.ofSeconds(25));

        Thread.sleep(2000);

        serialResultMsg.getBodies().sort((x, y) -> Math.min(x.getId(), y.getId()));
        distributedResultMsg.getBodies().sort((x, y) -> Math.min(x.getId(), y.getId()));

        for(int i = 0; i < totBodies; i++) {
            assertEquals(serialResultMsg.getBodies().get(i), distributedResultMsg.getBodies().get(i));
        }
    }

    @Test
    public void test1000steps100bodies() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 100;
        int maxIter = 1000;

        Behavior<ControllerMsg> controllerActor = ControllerActor.create(totBodies, maxIter, width, height, true);
        ActorSystem<ControllerMsg> system = ActorSystem.create(controllerActor, "ControllerActor");

        system.tell(new StartTest(probe.ref(), true));

        //TODO: cercare il valore di millisecondi (> 365)
        probe.expectMessageClass(DistributedTestResult.class, Duration.ofMillis(550));
    }

    @Test
    public void test1000steps1000bodies() {

    }

    @Test
    public void test1000steps5000bodies() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 5000;
        int maxIter = 1000;

        ActorSystem<ControllerMsg> system = ActorSystem.create(
                Behaviors.setup(
                        (ctx) -> {
                            ctx.spawn(ControllerActor.create(totBodies, maxIter, width, height, true),
                                    "controllerActor",
                                    MailboxSelector.fromConfig("my-app.priority-mailbox"));
                            return Behaviors.same();
                        }
                ), "system", ConfigFactory.load());


        system.tell(new StartTest(probe.ref(), true));

        probe.expectMessageClass(DistributedTestResult.class, Duration.ofMinutes(20));
    }

    @Test
    public void test10000steps100bodies() {

    }

    @Test
    public void test10000steps1000bodies() {

    }

    @Test
    public void test10000steps5000bodies() {

    }

    @Test
    public void test50000steps100bodies() {

    }

    @Test
    public void test50000steps1000bodies() {

    }

    @Test
    public void test50000steps5000bodies() {
        TestProbe<ValueMsg> probe = testKit.createTestProbe();

        int width = 620;
        int height = 620;
        int totBodies = 5000;
        int maxIter = 50000;

        ActorSystem<ControllerMsg> system = ActorSystem.create(
                Behaviors.setup(
                        (ctx) -> {
                            ctx.spawn(ControllerActor.create(totBodies, maxIter, width, height, true),
                                    "controllerActor",
                                    MailboxSelector.fromConfig("my-app.priority-mailbox"));
                            return Behaviors.same();
                        }
                ), "system", ConfigFactory.load());


        system.tell(new StartTest(probe.ref(), true));

        probe.expectMessageClass(DistributedTestResult.class, Duration.ofMinutes(140));
    }

}
