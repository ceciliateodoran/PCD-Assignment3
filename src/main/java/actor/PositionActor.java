package actor;

import actor.utils.Body;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

public class PositionActor extends AbstractBehavior<BodyMsg> {

    private PositionActor(final ActorContext<BodyMsg> context) {
        super(context);
    }

    @Override
    public Receive<BodyMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputePositionMsg.class, this::onComputePos)
                .build();
    }

    private Behavior<BodyMsg> onComputePos(final ComputePositionMsg msg) {
        //this.getContext().getLog().info("PositionActor: new position's computation message received from BodyActor.");

        /* compute bodies new pos */
        for (Body b : msg.getBodies()) {
            b.updatePos(0.001);
        }

        /* check collisions with boundaries */
        for (Body b : msg.getBodies()) {
            b.checkAndSolveBoundaryCollision(msg.getBounds());
        }

        msg.getReplyToBodyActor().tell(new PosUpdatedMsg(msg.getBodies()));

        return this;
    }

    public static Behavior<BodyMsg> create() {
        return Behaviors.setup(PositionActor::new);
    }

}