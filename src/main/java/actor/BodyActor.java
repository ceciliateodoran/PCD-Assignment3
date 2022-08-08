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


    private BodyActor(final ActorContext<BodyMsg> context) {
        super(context);
        this.initializeBodies(nBodies);
        this.createVelPosCalculators(context);
    }

    private void createVelPosCalculators(final ActorContext<BodyMsg> context) {
        this.velCalculatorRef = context.spawn(VelocityActor.create(), "velActor");
        this.posCalculatorRef = context.spawn(PositionActor.create(), "posActor");
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
        //this.getContext().getLog().info("BodyActor: message requesting new body values received from ControllerActor.");

        /*
        Dico al VelocityActor di calcolare i nuovi valori delle velocità dei bodies
         */
        this.velCalculatorRef.tell(new ComputeVelocityMsg(this.getContext().getSelf(), this.bodies));

        return this;
    }

    private Behavior<BodyMsg> onUpdatedVelocities(final VelUpdatedMsg msg) {
        //this.getContext().getLog().info("BodyActor: message containing the new velocity values received from VelocityActor.");

        /*
        Aggiorno i valori dei bodies con i nuovi valori di velocità calcolati dal VelocityActor
        e li mando al PositionActor
         */
        this.bodies = msg.getSplittedBodiesUpdated();
        this.posCalculatorRef.tell(new ComputePositionMsg(this.getContext().getSelf(), this.bodies, this.bounds));

        return this;
    }

    private Behavior<BodyMsg> onUpdatePositions(final PosUpdatedMsg msg) {
        //this.getContext().getLog().info("BodyActor: message containing the new position values received from PositionActor.");

        /*
        Aggiorno i valori dei bodies con i nuovi valori di posizione calcolati dal PositionActor
        e li mando al ControllerActor
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