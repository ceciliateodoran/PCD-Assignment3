package actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class BodyActor extends AbstractBehavior<BodyMsg> {

    public BodyActor(ActorContext<BodyMsg> context) {
        super(context);
    }
    @Override
    public Receive<BodyMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputePositionMsg.class, this::onNewIteration)
                .build();
    }

    private Behavior<BodyMsg> onNewIteration() {
        return this;
    }

    public static Behavior<BodyMsg> create() {
        return Behaviors.setup(BodyActor::new);
    }
}
