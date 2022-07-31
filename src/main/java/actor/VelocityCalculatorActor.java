package actor;

import actor.utils.Body;
import actor.utils.V2d;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;

public class VelocityCalculatorActor extends AbstractBehavior<VelocityCalculationMsg> {

    public VelocityCalculatorActor(ActorContext<VelocityCalculationMsg> context) {
        super(context);
    }

    @Override
    public Receive<VelocityCalculationMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputeVelocityMsg.class, this::onComputeVelocity)
                .build();
    }

    private Behavior<VelocityCalculationMsg> onComputeVelocity(ComputeVelocityMsg msg) {
        /* compute total force on bodies */
        V2d totalForce = computeTotalForceOnBody(msg.getBody(), msg.getBodies());

        /* compute instant acceleration */
        V2d acc = new V2d(totalForce).scalarMul(1.0 / msg.getBody().getMass());

        /* update velocity */
        msg.getBody().updateVelocity(acc, msg.getDt());

        msg.getPosActorRef().tell(new ComputeNewPositionMsg(msg.getReplyTo(), msg.getBody(),
                msg.getBodies(), msg.getDt(), msg.getBounds()));

        return this;
    }

    /* public factory to create the actor */
    public static Behavior<VelocityCalculationMsg> create() {
        return Behaviors.setup(VelocityCalculatorActor::new);
    }

    private V2d computeTotalForceOnBody(final Body b, final List<Body> bodies) {

        V2d totalForce = new V2d(0, 0);

        /* compute total repulsive force */
        for (int j = 0; j < bodies.size(); j++) {
            Body otherBody = bodies.get(j);
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
