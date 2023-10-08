package actor;

import actor.message.*;
import actor.utils.Body;
import actor.utils.Boundary;
import actor.utils.V2d;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import distributed.messages.spawn.Stop;

import java.util.List;
import java.util.stream.Collectors;

/**
 * attore che si occupa di calcolare i nuovi valori di velocità e posizione di ogni Body
 */
public class BodyActor extends AbstractBehavior<BodyMsg> {
    private final Body body;
    private static final double DISTANCE_FROM_BODY = 0.2;

    private double dt;
    private Boundary bounds;
    private ActorRef<ControllerMsg> replyTo;

    private BodyActor(final ActorContext<BodyMsg> context, final Body b) {
        super(context);
        this.body = b;
    }

    @Override
    public Receive<BodyMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputePositionsMsg.class, this::onComputationRequest)
                .onMessage(StopActor.class, this::onStop)
                .build();
    }

    private Behavior<BodyMsg> onStop(StopActor msg) {
        return Behaviors.stopped();
    }

    /* calcolo dei nuovi valori di velocità e posizione per ogni Body */
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

        msg.getReplyTo().tell(new BodyComputationResult(this.body));

        return this;
    }

    /* public factory to create Body actor */
    public static Behavior<BodyMsg> create(final Body b) {
        return Behaviors.setup(context -> new BodyActor(context, b));
    }

    private V2d computeTotalForceOnBody(final List<Body> bodies, final Body b) {
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