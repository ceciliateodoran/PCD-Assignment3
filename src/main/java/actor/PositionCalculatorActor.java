package actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class PositionCalculatorActor extends AbstractBehavior<PositionCalculatorMsg> {

    public PositionCalculatorActor(ActorContext<PositionCalculatorMsg> context) {
        super(context);
    }

    @Override
    public Receive<PositionCalculatorMsg> createReceive() {
        return null;
    }

    /* public factory to create the actor */
    public static Behavior<PositionCalculatorMsg> create() {
        return Behaviors.setup(PositionCalculatorActor::new);
    }
}
