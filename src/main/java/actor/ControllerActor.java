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

    public ControllerActor(ActorContext<ControllerMsg> context) {
        super(context);
        this.bodiesCounter = 0;
        this.currentIter = 0;
        context.spawn(BodyActor.create(totBodies), "bodyActor");
        context.spawn(PositionCalculatorActor.create(), "positionCalculatorActor");
        context.spawn(VelocityCalculatorActor.create(), "velocityCalculatorActor");
        //creare attore GUI

        // considerare che la GUI, quando viene premuto start, manda un messaggio
        // PositionsMsg al ControllerActor
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
            this.currentIter++;
            //ricominciare il calcolo
            msg.getReplyTo().tell(new ComputePositionMsg(this.getContext().getSelf()));
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
