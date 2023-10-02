package actor;

import actor.utils.Body;
import actor.utils.Boundary;
import actor.utils.V2d;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * attore che si occupa di calcolare i nuovi valori di velocità e posizione di ogni Body
 */
public class BodyActor extends AbstractBehavior<BodyMsg> {
    private Body body;
    private Map<ActorRef<BodyMsg>, Body> bodies;
    private int numReceivedBodies;
    private int totBodies;
    private double dt;
    private Boundary bounds;
    private ActorRef<ControllerMsg> replyTo;

    private BodyActor(final ActorContext<BodyMsg> context, final Body b) {
        super(context);
        this.body = b;
        this.numReceivedBodies = 0;
        this.totBodies = 0;
        this.dt = 0;
        this.bodies = new HashMap<>();
    }

    @Override
    public Receive<BodyMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputePositionsMsg.class, this::onNewIteration)
                .onMessage(StopMsg.class, this::onStop)
                .onMessage(RequestBody.class, this::onRequestBody)
                .onMessage(ResponseBody.class, this::onResponseBody)
                .onMessage(UpdateBody.class, this::onUpdateBody)
                .build();
    }

    private Behavior<BodyMsg> onUpdateBody(final UpdateBody msg) {
        this.body = msg.getBody();
        return this;
    }

    private void resetValues() {
        this.numReceivedBodies = 0;
        this.totBodies = 0;
        this.dt = 0;
        this.bodies = new HashMap<>();
    }

    private Behavior<BodyMsg> onResponseBody(final ResponseBody msg) {
        this.bodies.put(msg.getBodyActorRef(), msg.getBody());
        this.numReceivedBodies++;

        if (numReceivedBodies == totBodies - 1) {
            List<Body> bodyList = this.bodies.values().stream().toList();
            for (final Body b : bodyList) {
                /* compute total force on bodies */
                V2d totalForce = computeTotalForceOnBody(b);

                /* compute instant acceleration */
                V2d acc = new V2d(totalForce).scalarMul(1.0 / b.getMass());

                /* update velocity */
                b.updateVelocity(acc, this.dt);
            }

            /* compute bodies new pos */
            for (Body b : bodyList) {
                b.updatePos(this.dt);
            }

            /* check collisions with boundaries */
            for (Body b : bodyList) {
                b.checkAndSolveBoundaryCollision(this.bounds);
            }

            int index = 0;
            for (Map.Entry<ActorRef<BodyMsg>, Body> entry : this.bodies.entrySet()) {
                this.bodies.put(entry.getKey(), bodyList.get(index));
                index++;
            }
            this.bodies.forEach((key, value) -> key.tell(new UpdateBody(value)));

            this.replyTo.tell(new UpdatedPositionsMsg(this.bodies.values().stream().toList()));

            resetValues();
        }

        return this;
    }

    private Behavior<BodyMsg> onRequestBody(final RequestBody msg) {
        msg.getReplyTo().tell(new ResponseBody(this.body, getContext().getSelf()));
        return this;
    }

    /* calcolo dei nuovi valori di velocità e posizione per ogni Body */
    private Behavior<BodyMsg> onNewIteration(final ComputePositionsMsg msg) {
        //this.getContext().getLog().info("BodyActor: position's computation message received from ControllerActor.");
        this.totBodies = msg.getBodyRefsList().size();
        this.dt = msg.getDt();
        this.bounds = msg.getBounds();
        this.replyTo = msg.getReplyTo();
        msg.getBodyRefsList().stream().filter(ref -> !ref.equals(getContext().getSelf())).forEach(body -> body.tell(new RequestBody(getContext().getSelf())));
        return this;
    }

    /* reset dei bodies con nuovi valori (per un eventuale re-start) */
    private Behavior<BodyMsg> onStop(final StopMsg msg) {
        //this.getContext().getLog().info("BodyActor: iterations stop message received from ControllerActor.");
        return Behaviors.stopped();
    }

    /* public factory to create Body actor */
    public static Behavior<BodyMsg> create(final Body b) {
        return Behaviors.setup(context -> new BodyActor(context, b));
    }

    private V2d computeTotalForceOnBody(final Body b) {
        V2d totalForce = new V2d(0, 0);

        /* compute total repulsive force */
        for (Body otherBody : this.bodies.values()) {
            if (!b.equals(otherBody)) {
                try {
                    V2d forceByOtherBody = b.computeRepulsiveForceBy(otherBody);
                    totalForce.sum(forceByOtherBody);
                } catch (Exception ex) {
                    System.out.println("Error in force calculation of the body n." + bodies.values().stream().toList().indexOf(otherBody));
                    ex.printStackTrace();
                }
            }
        }

        /* add friction force */
        totalForce.sum(b.getCurrentFrictionForce());

        return totalForce;
    }
}