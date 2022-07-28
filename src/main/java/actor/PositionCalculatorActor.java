package actor;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

public class PositionCalculatorActor extends AbstractBehavior<ControllerMsg> {

    public PositionCalculatorActor(ActorContext<ControllerMsg> context) {
        super(context);
    }

    @Override
    public Receive<ControllerMsg> createReceive() {
        return null;
    }
}
