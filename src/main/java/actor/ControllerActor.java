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

    private ActorRef posCalcActorRef;

    private ActorRef velCalcActorRef;

    public ControllerActor(ActorContext<ControllerMsg> context) {
        super(context);
        this.bodiesCounter = 0;
        this.currentIter = 0;
        this.dt = 0.001;
        createActors(context);
        // considerare che la GUI, quando viene premuto start, manda un messaggio
        // PositionsMsg al ControllerActor
    }

    private void createActors(ActorContext<ControllerMsg> context) {
        context.spawn(BodyActor.create(this.getContext().getSelf(), totBodies), "bodyActor");
        this.posCalcActorRef = context.spawn(PositionCalculatorActor.create(), "positionCalculatorActor");
        this.velCalcActorRef = context.spawn(VelocityCalculatorActor.create(), "velocityCalculatorActor");
        //creare attore GUI
    }

    @Override
    public Receive<ControllerMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(PositionsMsg.class, this::onUpdatePos)
                .onMessage(GUIStopMsg.class, this::onStop)
                .build();
    }

    private Behavior<ControllerMsg> onUpdatePos(PositionsMsg msg) {
        this.getContext().getLog().info("newPos");
        if(this.bodiesCounter < this.totBodies && this.currentIter < this.maxIter){
            //inviare nuova posizione alla GUI
            this.bodiesCounter++;
        } else if (this.bodiesCounter == this.totBodies && this.currentIter < this.maxIter){
            this.bodiesCounter = 0;
            this.vt += this.dt;
            this.currentIter++;
            //ricominciare il calcolo
            msg.getReplyTo().tell(new ComputePositionMsg(this.getContext().getSelf(), this.posCalcActorRef, this.velCalcActorRef, this.dt));
        } else if (this.currentIter == this.maxIter) {
            //inviare fine iterazioni a GUI
        }

        return this;
    }

    private Behavior<ControllerMsg> onStop(GUIStopMsg msg) {
        this.getContext().getLog().info("setStop");
        // resetto i bodies
        msg.getReplyTo().tell(new StopMsg(this.getContext().getSelf()));
        return this;
    }

    /* public factory to create the actor */
    public static Behavior<ControllerMsg> create(int bodies, int iter) {
        totBodies = bodies;
        maxIter = iter;
        return Behaviors.setup(ControllerActor::new);
    }
}
