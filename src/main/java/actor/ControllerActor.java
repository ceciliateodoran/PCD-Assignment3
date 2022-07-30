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

    private int iter;

    private ActorRef<BodyMsg> bodyActor;

    private ActorRef<PositionCalculatorMsg> posCalcActor;

    private ActorRef<VelocityCalculatorMsg> velCalcActor;

    public ControllerActor(ActorContext<ControllerMsg> context) {
        super(context);
        this.bodiesCounter = 0;
        this.iter = 0;

        this.bodyActor = context.spawn(BodyActor.create(), "bodyActor");
        this.posCalcActor = context.spawn(PositionCalculatorActor.create(), "positionCalculatorActor");
        this.velCalcActor = context.spawn(VelocityCalculatorActor.create(), "velocityCalculatorActor");
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
        if(this.bodiesCounter < this.totBodies && this.iter < this.maxIter){
            //inviare nuova posizione alla GUI
            this.bodiesCounter++;
        } else if (this.bodiesCounter == this.totBodies && this.iter < this.maxIter){
            this.bodiesCounter = 0;
            this.iter++;
            msg.getReplyTo().tell(new ComputePositionMsg(this.getContext().getSelf()));
            //ricominciare il calcolo
        } else if (this.iter == this.maxIter) {
            //inviare fine iterazioni a GUI
/*            this.bodyActor.tell(new StopMsg(this.getContext().getSelf()));
            this.velCalcActor.tell(new StopMsg(this.getContext().getSelf()));
            this.posCalcActor.tell(new StopMsg(this.getContext().getSelf()));*/
        }

        return this;
    }

    private Behavior<ControllerMsg> onStop(GUIStopMsg msg) {
        this.getContext().getLog().info("setStop");
        return this;
    }

    /* public factory to create the actor */
    public static Behavior<ControllerMsg> create(int bodies, int iter) {
        totBodies = bodies;
        maxIter = iter;
        return Behaviors.setup(ControllerActor::new);
    }
}
