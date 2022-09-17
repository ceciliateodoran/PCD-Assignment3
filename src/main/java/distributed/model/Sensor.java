package distributed.model;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.actor.typed.ActorRef;
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

import java.time.Duration;
import java.util.Random;

public class Sensor extends AbstractActor {

    private int id;
    private int zone;
    private double value;

    /*public Sensor(final int id, final int z, final ServiceKey<ValueMsg> k) {
        this.id = id;
        this.zone = z;
        this.key = k;
        this.value = -1;
    }*/

    private void updateValue() {
        this.value = new Random().nextDouble();
    }

    public static Props props() {
        return Props.create(Sensor.class);
    }


    /**
     * Quando il Behavior del sensore invia un messaggio al sensore stesso,
     * esso legge/produce un nuovo valore e lo invia al coordinatore della propria zona
     *
     * @param msg - inviato dal Behavior del sensore
     * @return
     */
    private Behavior<ValueMsg> sendData(final RecordValueMsg msg) {
        LoggingAdapter log = getContext().getSystem().log();
        log.info("Sending message from sensor " + id + "via" + getContext().getSelf());
        this.updateValue();
        msg.getReplyToCoordinator().tell(new DetectedValueMsg(zone, id, value));
        return Behaviors.same();
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "id=" + id +
                ", zone=" + zone +
                ", value=" + value +
                '}';
    }

    @Override
    public Receive createReceive() {
        /*
         * Viene creato il sensore specificandogli questo comportamento:
         *   il seguente Behavior una volta impostato è tale da "attivare il sensore" tramite un messaggio
         *   (ValueMsg) che invia ogni N millisecondi
         * */
        /*return Behaviors.setup(
                (ActorContext<ValueMsg> context) -> {
                    Sensor s = new Sensor(context, id, z, key);
                    context.getSystem()
                            .receptionist()
                            .tell(Receptionist.register(key, context.getSelf()));
                    return Behaviors.withTimers(
                            t -> {
                                t.startTimerAtFixedRate(new RecordValueMsg(coordinator), Duration.ofMillis(3000));
                                return s.behavior();
                            }
                    );
                }
        );*/
        return null;
    }
}

