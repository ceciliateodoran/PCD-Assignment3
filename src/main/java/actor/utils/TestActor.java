package actor.utils;

import actor.message.test.FakeIterationCompleted;
import actor.message.test.FakeUpdatePositionMsg;
import actor.message.test.SerialTestResult;
import actor.message.test.StartTest;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import distributed.messages.ValueMsg;

import java.util.List;

public class TestActor extends AbstractBehavior<ValueMsg> {
    private static List<Body> bodiesList;
    private static Boundary boundValues;
    private static long steps;
    private static double dt;
    private double vt;

    private TestActor(final ActorContext<ValueMsg> context) {
        super(context);
        this.vt = 0;
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartTest.class, this::onReceiveStartMessage)
                .onMessage(FakeUpdatePositionMsg.class, this::onFakeUpdatePositionMessage)
                .build();
    }

    public static Behavior<ValueMsg> create(final List<Body> bodies, final Boundary bounds, final long nSteps, final double deltaT) {
        bodiesList = bodies;
        boundValues = bounds;
        steps = nSteps;
        dt = deltaT;
        return Behaviors.setup(TestActor::new);
    }

    private Behavior<ValueMsg> onReceiveStartMessage(StartTest msg) {
        int iter = 0;

        /* simulation loop */
        while (iter < steps) {
            /* update bodies velocity */
            for (int i = 0; i < bodiesList.size(); i++) {
                Body b = bodiesList.get(i);

                /* compute total force on bodies */
                V2d totalForce = computeTotalForceOnBody(b);

                /* compute instant acceleration */
                V2d acc = new V2d(totalForce).scalarMul(1.0 / b.getMass());

                /* update velocity */
                b.updateVelocity(acc, dt);
            }

            /* compute bodies new pos */
            for (Body b : bodiesList) {
                b.updatePos(dt);

            }

            /* check collisions with boundaries */
            for (Body b : bodiesList) {
                b.checkAndSolveBoundaryCollision(boundValues);
            }

            /* update virtual time */
            vt = vt + dt;
            iter++;
        }

        msg.getRef().tell(new SerialTestResult(bodiesList));

        return this;
    }

    private Behavior<ValueMsg> onFakeUpdatePositionMessage(FakeUpdatePositionMsg msg) {
        msg.getControllerRef().tell(new FakeIterationCompleted());
        return this;
    }

    private V2d computeTotalForceOnBody(final Body b) {

        V2d totalForce = new V2d(0, 0);

        /* compute total repulsive force */
        for (int j = 0; j < bodiesList.size(); j++) {
            Body otherBody = bodiesList.get(j);
            if (!b.equals(otherBody)) {
                try {
                    V2d forceByOtherBody = b.computeRepulsiveForceBy(otherBody);
                    totalForce.sum(forceByOtherBody);
                } catch (Exception ex) {
                }
            }
        }

        /* add friction force */
        totalForce.sum(b.getCurrentFrictionForce());

        return totalForce;
    }
}
