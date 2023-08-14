package actor;

import actor.utils.Body;
import actor.utils.BodyGenerator;
import actor.utils.Boundary;
import actor.utils.V2d;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;

/**
 * attore che si occupa di calcolare i nuovi valori di velocità e posizione di ogni Body
 */
public class BodyActor extends AbstractBehavior<BodyMsg> {
    private static int nBodies;

    private Boundary bounds;

    private List<Body> bodies;

    private BodyActor(final ActorContext<BodyMsg> context) {
        super(context);
        this.initializeBodies(nBodies);
    }

    @Override
    public Receive<BodyMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputePositionsMsg.class, this::onNewIteration)
                .onMessage(StopMsg.class, this::onStop)
                .build();
    }

    /* calcolo dei nuovi valori di velocità e posizione per ogni Body */
    private Behavior<BodyMsg> onNewIteration(final ComputePositionsMsg msg) {
        //this.getContext().getLog().info("BodyActor: position's computation message received from ControllerActor.");

        for (int i = 0; i < this.bodies.size(); i++) {
            Body b = this.bodies.get(i);

            /* compute total force on bodies */
            V2d totalForce = computeTotalForceOnBody(b);

            /* compute instant acceleration */
            V2d acc = new V2d(totalForce).scalarMul(1.0 / b.getMass());

            /* update velocity */
            b.updateVelocity(acc, msg.getDt());
        }

        /* compute bodies new pos */
        for (Body b : this.bodies) {
            b.updatePos(msg.getDt());
        }

        /* check collisions with boundaries */
        for (Body b : this.bodies) {
            b.checkAndSolveBoundaryCollision(this.bounds);
        }

        msg.getReplyTo().tell(new UpdatedPositionsMsg(this.bodies, this.bounds));

        return this;
    }

    /* reset dei bodies con nuovi valori (per un eventuale re-start) */
    private Behavior<BodyMsg> onStop(final StopMsg msg) {
        //this.getContext().getLog().info("BodyActor: iterations stop message received from ControllerActor.");
        this.initializeBodies(nBodies);

        return this;
    }

    /* public factory to create Body actor */
    public static Behavior<BodyMsg> create(final int totBodies) {
        nBodies = totBodies;
        return Behaviors.setup(BodyActor::new);
    }

    private void initializeBodies(final int totBodies) {
        this.bounds =  new Boundary(-6.0, -6.0, 6.0, 6.0);
        BodyGenerator bg = new BodyGenerator();
        this.bodies = bg.generateBodies(totBodies, this.bounds);
    }

    private V2d computeTotalForceOnBody(final Body b) {
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