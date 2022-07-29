package actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class VelocityCalculatorActor extends AbstractBehavior<VelocityCalculatorMsg> {

    public VelocityCalculatorActor(ActorContext<VelocityCalculatorMsg> context) {
        super(context);
    }

    @Override
    public Receive<VelocityCalculatorMsg> createReceive() {
        return null;
    }

    /* public factory to create the actor */
    public static Behavior<VelocityCalculatorMsg> create() {
        return Behaviors.setup(VelocityCalculatorActor::new);
    }
}
