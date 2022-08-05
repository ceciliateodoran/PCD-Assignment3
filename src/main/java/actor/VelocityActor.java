package actor;

import actor.utils.Body;
import actor.utils.V2d;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;

public class VelocityActor extends AbstractBehavior<BodyMsg> {


    public VelocityActor(final ActorContext<BodyMsg> context) {
        super(context);
    }

    @Override
    public Receive<BodyMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputeVelocityMsg.class, this::onComputeVel)
                .build();
    }

    private Behavior<BodyMsg> onComputeVel(final ComputeVelocityMsg msg) {
        //this.getContext().getLog().info("VelocityActor: new velocity's computation message received from BodyActor.");

        for (int i = 0; i < msg.getBodies().size(); i++) {
            Body b = msg.getBodies().get(i);

            /* compute total force on bodies */
            V2d totalForce = computeTotalForceOnBody(b, msg.getBodies());

            /* compute instant acceleration */
            V2d acc = new V2d(totalForce).scalarMul(1.0 / b.getMass());

            /* update velocity */
            b.updateVelocity(acc, 0.001);
        }

        msg.getReplyToBodyActor().tell(new VelUpdatedMsg(msg.getBodies()));

        return this;
    }

    public static Behavior<BodyMsg> create() {
        return Behaviors.setup(VelocityActor::new);
    }

    private V2d computeTotalForceOnBody(final Body b, final List<Body> bodies) {
        V2d totalForce = new V2d(0, 0);

        /* compute total repulsive force */
        for (Body otherBody : bodies) {
            if (!b.equals(otherBody)) {
                try {
                    V2d forceByOtherBody = b.computeRepulsiveForceBy(otherBody);
                    totalForce.sum(forceByOtherBody);
                } catch (Exception ex) {
                    System.out.println("Error in force calculation of the body n." + bodies.indexOf(otherBody));
                    ex.printStackTrace();
                }
            }
        }

        /* add friction force */
        totalForce.sum(b.getCurrentFrictionForce());

        return totalForce;
    }
}
