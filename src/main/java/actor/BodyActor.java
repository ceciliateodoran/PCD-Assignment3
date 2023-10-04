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

import java.util.ArrayList;
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
    private int totBodies;
    private double dt;
    private Boundary bounds;
    private ActorRef<ControllerMsg> replyTo;

    private BodyActor(final ActorContext<BodyMsg> context, final Body b) {
        super(context);
        this.body = b;
        this.totBodies = 0;
    }

    @Override
    public Receive<BodyMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputePositionsMsg.class, this::onNewIteration)
                .onMessage(StopMsg.class, this::onStop)
                .onMessage(UpdateBody.class, this::onUpdateBody)
                .build();
    }

    private Behavior<BodyMsg> onUpdateBody(final UpdateBody msg) {
        this.body = msg.getBody();
        return this;
    }

    private void resetValues() {
        this.totBodies = 0;
        this.dt = 0;
    }

    /* calcolo dei nuovi valori di velocità e posizione per ogni Body */
    private Behavior<BodyMsg> onNewIteration(final ComputePositionsMsg msg) {
        Map<Integer, Body> indexBodyMap = new HashMap<>();

        this.totBodies = msg.getBodyList().size();
        this.dt = msg.getDt();
        this.bounds = msg.getBounds();
        this.replyTo = msg.getReplyTo();

        List<Body> bodyList = msg.getBodyList();
        for (final Body b : bodyList) {
            /* compute total force on bodies */
            V2d totalForce = computeTotalForceOnBody(bodyList, b);

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
            indexBodyMap.put(bodyList.indexOf(b), b);
        }

        this.replyTo.tell(new UpdatedPositionsMsg(indexBodyMap));

        resetValues();

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