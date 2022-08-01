package actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ControllerActor extends AbstractBehavior<ControllerMsg> {
    private static int totBodies;

    private static int maxIter;

    private int bodiesCounter;

    private int currentIter;

    private double vt;

    /* virtual time step */
    private final double dt;

    private ActorRef<BodyMsg> bodyActorRef;

    private ActorRef<PositionCalculationMsg> posCalcActorRef;

    private ActorRef<VelocityCalculationMsg> velCalcActorRef;

    public ControllerActor(final ActorContext<ControllerMsg> context) {
        super(context);
        this.dt = 0.001;
        resetCounters();
        createActors(context);
        // considerare che la GUI, quando viene premuto start, manda un messaggio
        // PositionsMsg al ControllerActor
    }

    private void createActors(final ActorContext<ControllerMsg> context) {
        this.bodyActorRef = context.spawn(BodyActor.create(this.getContext().getSelf(), totBodies), "bodyActor");
        this.posCalcActorRef = context.spawn(PositionCalculatorActor.create(), "positionCalculatorActor");
        this.velCalcActorRef = context.spawn(VelocityCalculatorActor.create(), "velocityCalculatorActor");
        // invio il messaggio di start al BodyActor
        this.bodyActorRef.tell(new ComputePositionMsg(this.posCalcActorRef, this.velCalcActorRef, this.dt));
        //creare attore GUI
    }

    @Override
    public Receive<ControllerMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(PositionsMsg.class, this::onUpdatePos)
                .onMessage(GUIStopMsg.class, this::onStop)
                .build();
    }

    private Behavior<ControllerMsg> onUpdatePos(final PositionsMsg msg) {
        this.getContext().getLog().info("ControllerActor: message of start pos calculation received.");
        // Debug
        // System.out.println("bodiesCounter: " + this.bodiesCounter);
        // System.out.println("currentIter: " + this.currentIter);
        if (this.bodiesCounter < totBodies && this.currentIter < maxIter) {
            //inviare nuova posizione alla GUI
            this.bodiesCounter++;
            if (this.bodiesCounter == totBodies) {
                this.bodiesCounter = 0;
                this.vt += this.dt;
                this.currentIter++;
                if (this.currentIter == maxIter) {
                    //inviare fine iterazioni a GUI
                } else {
                    //ricominciare il calcolo
                    this.bodyActorRef.tell(new ComputePositionMsg(this.posCalcActorRef, this.velCalcActorRef, this.dt));
                }
            }
        }

        return this;
    }

    private Behavior<ControllerMsg> onStop(final GUIStopMsg msg) {
        this.getContext().getLog().info("ControllerActor: stop message received from GUI.");
        resetCounters();
        // resetto i bodies
        this.bodyActorRef.tell(new StopMsg(this.getContext().getSelf()));
        return this;
    }

    private void resetCounters() {
        this.bodiesCounter = 0;
        this.currentIter = 0;
    }

    /* public factory to create the actor */
    public static Behavior<ControllerMsg> create(final int bodies, final int iter) {
        totBodies = bodies;
        maxIter = iter;
        return Behaviors.setup(ControllerActor::new);
    }
}
