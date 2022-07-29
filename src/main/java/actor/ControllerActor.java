package actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class ControllerActor extends AbstractBehavior<ControllerMsg>{
    private static int totBodies;
    private static int maxIter;
    private int bodiesCounter;
    private int iter;

    public ControllerActor(ActorContext<ControllerMsg> context) {
        super(context);
        this.bodiesCounter = 0;
        this.iter = 0;
        //creare attori x body, position, velocity, GUI
    }

    @Override
    public Receive<ControllerMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(PositionsMsg.class, this::onUpdatePos)
                .onMessage(StopMsg.class, this::onStop)
                .build();
    }

    private Behavior<ControllerMsg> onUpdatePos(PositionsMsg msg) {
        this.getContext().getLog().info("newPos");
        if(this.bodiesCounter < this.totBodies && this.iter < this.maxIter){
            //inviare nuova posizione alla GUI
            this.bodiesCounter++;
        }else if (this.bodiesCounter == this.totBodies){
            this.bodiesCounter = 0;
        }

        return this;
    }

    private Behavior<ControllerMsg> onStop(StopMsg msg) {
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
