package actor;

import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Receive;

public class BodyActor extends AbstractBehavior<ControllerMsg> {

    public BodyActor(ActorContext<ControllerMsg> context) {
        super(context);
    }

    @Override
    public Receive<ControllerMsg> createReceive() {
        return null;
    }
}
