package actor;

import actor.view.*;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ControllerActor extends AbstractBehavior<ControllerMsg> {
    private static final int UPDATE_FREQUENCY = 2;

    private static int totBodies;

    private static int maxIter;

    private static int viewHeight;

    private static int viewWidth;

    private int currentIter;

    private double vt;

    /* virtual time step */
    private final double dt;

    private ActorRef<BodyMsg> bodyActorRef;

    private ActorRef<ViewMsg> viewActorRef;

    public ControllerActor(final ActorContext<ControllerMsg> context) {
        super(context);
        this.dt = 0.001;
        resetCounters();
        createActors(context);
    }

    private void createActors(final ActorContext<ControllerMsg> context) {
        this.bodyActorRef = context.spawn(BodyActor.create(context.getSelf(), totBodies), "bodyActor");

        // invio del messaggio di start al BodyActor (versione NO GUI)
        // this.bodyActorRef.tell(new ComputePositionMsg(this.posCalcActorRef, this.velCalcActorRef, this.dt));

        // creazione della GUI
        this.viewActorRef = context.spawn(ViewActor.create(context.getSelf(), viewWidth, viewHeight), "viewActor");
    }

    @Override
    public Receive<ControllerMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(PositionsMsg.class, this::onUpdatePos)
                .onMessage(ViewStopMsg.class, this::onStop)
                .onMessage(ViewUpdatedMsg.class, this::onViewUpdated)
                .onMessage(ViewStartMsg.class, this::onViewStart)
                .build();
    }

    private Behavior<ControllerMsg> onViewStart(final ViewStartMsg msg) {
        this.bodyActorRef.tell(new ComputePositionMsg(this.getContext().getSelf(), this.dt));
        return this;
    }

    private Behavior<ControllerMsg> onUpdatePos(final PositionsMsg msg) {
        //this.getContext().getLog().info("ControllerActor: message of start pos calculation received.");
        // Debug
        // System.out.println("bodiesCounter: " + this.bodiesCounter);
        // System.out.println("currentIter: " + this.currentIter);
        if (this.currentIter < maxIter) {
            this.vt += this.dt;
            this.currentIter++;

            //if(this.currentIter % UPDATE_FREQUENCY == 0){
            this.viewActorRef.tell(new PositionsMsg(msg.getBodies(), this.vt, this.currentIter, msg.getBounds()));
            //}

            /**
             * No-GUI version
             */
            /*if (this.currentIter == maxIter) {
                // reset
                resetCounters();
                this.bodyActorRef.tell(new StopMsg(this.getContext().getSelf()));

                //inviare msg di fine iterazioni a GUI
                this.viewActorRef.tell(new ControllerStopMsg());
            } else {
                //ricominciare il calcolo
                this.bodyActorRef.tell(new ComputePositionMsg(this.getContext().getSelf(), this.dt));
            }*/
        }
        return this;
    }

    private Behavior<ControllerMsg> onViewUpdated(final ViewUpdatedMsg msg) {
        if (this.currentIter == maxIter) {
            // reset
            resetCounters();
            this.bodyActorRef.tell(new StopMsg(this.getContext().getSelf()));

            //inviare msg di fine iterazioni a GUI
            this.viewActorRef.tell(new ControllerStopMsg());
        } else {
            //ricominciare il calcolo
            this.bodyActorRef.tell(new ComputePositionMsg(this.getContext().getSelf(), this.dt));
        }
        return this;
    }

    private Behavior<ControllerMsg> onStop(final ViewStopMsg msg) {
        //this.getContext().getLog().info("ControllerActor: stop message received from GUI.");
        resetCounters();
        // reset dei bodies
        this.bodyActorRef.tell(new StopMsg(this.getContext().getSelf()));
        return this;
    }

    private void resetCounters() {
        this.currentIter = 0;
        this.vt = 0;
    }

    /* public factory to create the actor */
    public static Behavior<ControllerMsg> create(final int bodies, final int iter, final int w, final int h) {
        totBodies = bodies;
        maxIter = iter;
        viewHeight = h;
        viewWidth = w;
        return Behaviors.setup(ControllerActor::new);
    }
}
