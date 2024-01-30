package actor;

import actor.message.*;
import actor.utils.Body;
import actor.utils.V2d;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represent the Body actor implementation,
 * Compute the new velocity and position values
 */
public class BodyActor extends AbstractBehavior<BodyMsg> {
    private static final double DISTANCE_FROM_BODY = 0.2;
    private final List<Body> bodies;

    private BodyActor(final ActorContext<BodyMsg> context, final List<Body> b) {
        super(context);
        this.bodies = b;
    }

    /**
     * Construct a new instance of the Body actor
     *
     * @param b The body
     * @return The newly created instance of the Body actor
     */
    public static Behavior<BodyMsg> create(final List<Body> b) {
        return Behaviors.setup(context -> new BodyActor(context, b));
    }

    @Override
    public Receive<BodyMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputePositionsMsg.class, this::onComputationRequest)
                .onMessage(StopActorMsg.class, this::onStop)
                .build();
    }

    //termination of the actor
    private Behavior<BodyMsg> onStop(final StopActorMsg msg) {
        return Behaviors.stopped();
    }

    //computation of the new velocity and position values for each Body
    private Behavior<BodyMsg> onComputationRequest(final ComputePositionsMsg msg) {
        for(Body body : this.bodies) {


            /* compute total force on bodies */
            V2d totalForce = computeTotalForceOnBody(msg.getBodyList().stream()
                    .filter(otherBody -> Math.abs(otherBody.getPos().getX() - body.getPos().getX()) < 0.2 &&
                            Math.abs(otherBody.getPos().getY() - body.getPos().getY()) < 0.2 &&
                            otherBody.getDistanceFrom(body) <= DISTANCE_FROM_BODY).collect(Collectors.toList()), body);

            /* compute instant acceleration */
            V2d acc = new V2d(totalForce).scalarMul(1.0 / body.getMass());

            /* update velocity */
            body.updateVelocity(acc, msg.getDt());

            /* compute bodies new pos */
            body.updatePos(msg.getDt());

            /* check collisions with boundaries */
            body.checkAndSolveBoundaryCollision(msg.getBounds());
        }

        msg.getReplyTo().tell(new BodyComputationResultMsg(this.bodies, msg.getRunNumber()));
        return this;
    }

    private V2d computeTotalForceOnBody(final List<Body> bodies, final Body b) {
        V2d totalForce = new V2d(0, 0);

        // compute total repulsive force
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
        // add friction force
        totalForce.sum(b.getCurrentFrictionForce());
        return totalForce;
    }
}