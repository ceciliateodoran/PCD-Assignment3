package distributed.model;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.event.LoggingAdapter;
import distributed.messages.DetectedValueMsg;
import distributed.messages.RecordValueMsg;
import distributed.messages.ValueMsg;
import org.slf4j.Logger;

public class CoordinatorZone extends AbstractActor {

    private static int zone;

    /*public CoordinatorZone(final int z) {
        this.zone = z;
    }*/

    public static Props props() {
        return Props.create(CoordinatorZone.class);
    }

    @Override
    public Receive createReceive() {
        return null;
    }

    private Behavior evaluateData(final DetectedValueMsg msg) {
        LoggingAdapter log = getContext().getSystem().log();
        log.info("Message received from Coordinator" + zone + " : " + msg.toString());
        return Behaviors.same();
    }
}
