package actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import actor.utils.Body;

import java.util.ArrayList;

public class BodyActor extends AbstractBehavior<BodyMsg> {

    private ArrayList<Body> bodies;

    public BodyActor(ActorContext<BodyMsg> context) {
        super(context);
    }

    @Override
    public Receive<BodyMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputePositionMsg.class, this::onNewIteration)
                .onMessage(UpdatePositionMsg.class, this::onNewPosition)
                .build();
    }

    /* manda messaggio a VelocityCalculator per iniziare a calcolare i nuovi valori */
    private Behavior<BodyMsg> onNewIteration(ComputePositionMsg msg) {
        return this;
    }

    /* aggiorna i valori delle nuove posizioni calcolate dal PositionCalculator */
    private Behavior<BodyMsg> onNewPosition(UpdatePositionMsg msg) {
        return this;
    }

    /* public factory to create the actor */
    public static Behavior<BodyMsg> create() {
        return Behaviors.setup(BodyActor::new);
    }
}
