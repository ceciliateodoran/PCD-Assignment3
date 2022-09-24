package distributed.model;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import distributed.messages.DetectedValueMsg;
import distributed.messages.ValueMsg;
import org.slf4j.Logger;

public class CoordinatorZone extends AbstractBehavior<ValueMsg> {

    private int zone;

    private CoordinatorZone(final ActorContext<ValueMsg> context, final int z) {
        super(context);
        this.zone = z;
    }

    public static Behavior<ValueMsg> create(final int z) {
        return Behaviors.setup(ctx -> new CoordinatorZone(ctx, z));
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(DetectedValueMsg.class, this::evaluateData)
                .build();
    }

    private Behavior<ValueMsg> evaluateData(final DetectedValueMsg msg) {
        Logger log = this.getContext().getSystem().log();
        log.info("Message received from Coordinator" + this.zone + " : " + msg.toString());
        return Behaviors.same();
    }
}
