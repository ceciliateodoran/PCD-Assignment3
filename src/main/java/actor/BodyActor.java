package actor;

import actor.utils.Body;
import actor.utils.BodyGenerator;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;
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

    private ActorRef velCalculatorRef;

    private ActorRef posCalculatorRef;

    private static ActorRef controllerRef;


    public BodyActor(final ActorContext<BodyMsg> context) {
        super(context);
        this.initializeBodies(nBodies);
        this.createVelPosCalculators(context);
    }

    private void createVelPosCalculators(final ActorContext<BodyMsg> context) {
        this.velCalculatorRef = context.spawn(VelocityCalculatorActor.create(), "velActor");
        this.posCalculatorRef = context.spawn(PositionCalculatorActor.create(), "posActor");
    }

    @Override
    public Receive<BodyMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputePositionsMsg.class, this::onNewIteration)
                .onMessage(StopMsg.class, this::onStop)
                .onMessage(PosUpdatedMsg.class, this::onUpdatePositions)
                .onMessage(VelUpdatedMsg.class, this::onUpdatedVelocities)
                .build();
    }

    /* calcolo dei nuovi valori di velocità e posizione per ogni Body */
    private Behavior<BodyMsg> onNewIteration(final ComputePositionsMsg msg) {
        //this.getContext().getLog().info("BodyActor: position's computation message received from ControllerActor.");

        /**
         * Mando messaggi ai Vel
         */

        this.velCalculatorRef.tell(new ComputeVelocityMsg(this.getContext().getSelf(), this.bodies));

        /**
         * Old part
         */

        /*for (int i = 0; i < this.bodies.size(); i++) {
            Body b = this.bodies.get(i);

            *//* compute total force on bodies *//*
            V2d totalForce = computeTotalForceOnBody(b);

            *//* compute instant acceleration *//*
            V2d acc = new V2d(totalForce).scalarMul(1.0 / b.getMass());

            *//* update velocity *//*
            b.updateVelocity(acc, msg.getDt());
        }

        *//* compute bodies new pos *//*
        for (Body b : this.bodies) {
            b.updatePos(msg.getDt());
        }

        *//* check collisions with boundaries *//*
        for (Body b : this.bodies) {
            b.checkAndSolveBoundaryCollision(this.bounds);
        }

        msg.getReplyTo().tell(new UpdatedPositionsMsg(this.bodies, this.bounds));*/

        return this;
    }

    private Behavior<BodyMsg> onUpdatedVelocities(final VelUpdatedMsg msg) {
        /**
         * Aspetto risposte da tutti i Vel
         * Mando messaggi ai Pos
         */

        this.bodies = msg.getSplittedBodiesUpdated();
        this.posCalculatorRef.tell(new ComputePositionMsg(this.getContext().getSelf(), this.bodies, this.bounds));

        return this;
    }

    private Behavior<BodyMsg> onUpdatePositions(final PosUpdatedMsg msg) {

        /**
         * Aspetto risposte da tutti i Pos
         * Mando messaggio al BodyActor
         */

        this.bodies = msg.getSplittedBodiesUpdated();
        controllerRef.tell(new UpdatedPositionsMsg(this.bodies, this.bounds));

        return this;
    }

    /* reset dei bodies con nuovi valori (per un eventuale re-start) */
    private Behavior<BodyMsg> onStop(final StopMsg msg) {
        //this.getContext().getLog().info("BodyActor: iterations stop message received from ControllerActor.");
        this.initializeBodies(nBodies);

        return this;
    }

    /* public factory to create Body actor */
    public static Behavior<BodyMsg> create(final ActorRef ctrlRef, final int totBodies) {
        nBodies = totBodies;
        controllerRef = ctrlRef;
        return Behaviors.setup(BodyActor::new);
    }

    private void initializeBodies(final int totBodies) {
        this.bounds =  new Boundary(-6.0, -6.0, 6.0, 6.0);
        BodyGenerator bg = new BodyGenerator();
        this.bodies = bg.generateBodies(totBodies, this.bounds);
    }
}