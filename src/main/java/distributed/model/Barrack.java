package distributed.model;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.pubsub.Topic;
import distributed.messages.BarrackStatus;
import distributed.messages.DetectedValueMsg;
import distributed.messages.ValueMsg;
import distributed.messages.ZoneStatus;
import org.slf4j.Logger;

public class Barrack extends AbstractBehavior<ValueMsg> {

    private static int zone;
    private String status;
    private Boolean isSilenced;
    private final String GUIAddress;

    private Barrack(final ActorContext<ValueMsg> context, final int z, final String GUIaddress) {
        super(context);
        this.zone = z;
        this.GUIAddress = GUIaddress;
    }

    //
    public static Behavior<ValueMsg> create(final int z, final ActorRef<Topic.Command<ValueMsg>> topic, final String GUIaddress) {
        return Behaviors.setup(ctx -> {
            topic.tell(Topic.subscribe(ctx.getSelf()));
            return new Barrack(ctx, z, GUIaddress);
        });
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ZoneStatus.class, this::evaluateData)
                .build();
    }

    private Behavior<ValueMsg> evaluateData(final ZoneStatus msg) {
        Logger log = this.getContext().getSystem().log();
        log.info("Message received from Coordinator" + zone + " : " + msg.toString());
        if(msg.getStatus() == "OK" || isSilenced){
            //do nothing
        } else {
            this.topicGUI.tell(Topic.publish(new BarrackStatus(msg.getStatus(), msg.getDateTimeStamp(), msg.getSnapshot())));
        }
        return this;
    }
}
