package actor;

import actor.utils.Body;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class PositionCalculatorActor extends AbstractBehavior<PositionCalculationMsg> {

    public PositionCalculatorActor(final ActorContext<PositionCalculationMsg> context) {
        super(context);
    }

    @Override
    public Receive<PositionCalculationMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputeNewPositionMsg.class, this::onComputePosition)
                .build();
    }

    private Behavior<PositionCalculationMsg> onComputePosition(final ComputeNewPositionMsg msg) {
        this.getContext().getLog().info("PositionCalculatorActor: position's computation message received from VelocityCalculatorActor.");
        int index = msg.getBodies().indexOf(msg.getCurrentBody());

        /* compute bodies new pos */
        for (Body b : msg.getBodies()) {
            b.updatePos(msg.getDt());
        }

        /* check collisions with boundaries */
        for (Body b : msg.getBodies()) {
            b.checkAndSolveBoundaryCollision(msg.getBounds());
        }

        msg.getReplyTo().tell(new UpdatePositionMsg(msg.getCurrentBody(), index, msg.getDt()));

        return this;
    }

    /* public factory to create the actor */
    public static Behavior<PositionCalculationMsg> create() {
        return Behaviors.setup(PositionCalculatorActor::new);
    }
}
