package distributed.model;


import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.japi.Pair;
import distributed.messages.DetectedValueMsg;
import distributed.messages.UpdateSelfStatusMsg;
import distributed.messages.RequestSensorDataMsg;
import distributed.messages.ValueMsg;

import java.time.Duration;
import java.util.Random;

public class Sensor extends AbstractBehavior<ValueMsg> {
    private String id;
    private int zone;
    private double value;
    private final double limit;
    private Pair<Integer, Integer> spaceCoords;
    private static final IdGenerator idGenerator = new IdGenerator();

    public Sensor(final ActorContext<ValueMsg> context, final String id, final int z, final Pair<Integer, Integer> sc, double limit) {
        super(context);
        this.id = id;
        this.zone = z;
        this.spaceCoords = sc;
        this.limit = limit;
        this.value = -1;
    }

    private void updateValue() {
        Long upperLimit = Math.round(this.limit + this.limit/2);
        this.value = new Random().nextInt(Integer.parseInt(upperLimit.toString()));
    }

    public static Behavior<ValueMsg> create(final String id, final int z, final Pair<Integer, Integer> sc, double limit) {
        /*
         * Viene creato il sensore specificandogli questo comportamento:
         *   il seguente Behavior una volta impostato è tale da "attivare il sensore" tramite un messaggio
         *   (ValueMsg) che invia ogni N millisecondi
         * */
        return Behaviors.setup(
            context -> {
                Sensor s = new Sensor(context, id, z, sc, limit);
                //subscribe to receptionist
                context.getSystem()
                        .receptionist()
                        .tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGenerator.getSensorsKey(z)), context.getSelf()));
                return Behaviors.withTimers(
                        t -> {
                            t.startTimerAtFixedRate(new UpdateSelfStatusMsg(), Duration.ofMillis(10000));
                            return s;
                        }
                );
            }
        );
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(UpdateSelfStatusMsg.class, this::updateData)
                .onMessage(RequestSensorDataMsg.class, this::configureIterationAndSend)
                .build();
    }

    private Behavior<ValueMsg> configureIterationAndSend(RequestSensorDataMsg msg){
        System.out.println("Sending message from sensor " + id);
        msg.getReplyTo().tell(new DetectedValueMsg(zone, id, value, this.limit, this.spaceCoords, msg.getSeqNumber()));
        return Behaviors.same();
    }
    /**
     * Quando il Behavior del sensore invia un messaggio al sensore stesso,
     * esso legge/produce un nuovo valore e lo invia al coordinatore della propria zona
     *
     * @param msg - inviato dal Behavior del sensore
     * @return
     */
    private Behavior<ValueMsg> updateData(final UpdateSelfStatusMsg msg) {
        this.updateValue();
        return Behaviors.same();
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "id='" + id + '\'' +
                ", zone=" + zone +
                ", value=" + value +
                ", limit=" + limit +
                ", spaceCoords=" + spaceCoords +
                '}';
    }
}
