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
    private final Body body;

    private BodyActor(final ActorContext<BodyMsg> context, final Body b) {
        super(context);
        this.body = b;
    }

    /**
     * Construct a new instance of the Body actor
     *
     * @param b The body
     * @return The newly created instance of the Body actor
     */
    public static Behavior<BodyMsg> create(final Body b) {
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

        /* compute total force on bodies */
        V2d totalForce = computeTotalForceOnBody(msg.getBodyList().stream()
                .filter(body -> Math.abs(body.getPos().getX() - this.body.getPos().getX()) < 0.2 &&
                        Math.abs(body.getPos().getY() - this.body.getPos().getY()) < 0.2 &&
                        body.getDistanceFrom(this.body) <= DISTANCE_FROM_BODY).collect(Collectors.toList()), this.body);

        /* compute instant acceleration */
        V2d acc = new V2d(totalForce).scalarMul(1.0 / this.body.getMass());

        /* update velocity */
        this.body.updateVelocity(acc, msg.getDt());

        /* compute bodies new pos */
        this.body.updatePos(msg.getDt());

        /* check collisions with boundaries */
        this.body.checkAndSolveBoundaryCollision(msg.getBounds());

        msg.getReplyTo().tell(new BodyComputationResultMsg(this.body, msg.getRunNumber()));
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