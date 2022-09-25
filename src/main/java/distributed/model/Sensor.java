package distributed.model;


import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.japi.Pair;
import distributed.messages.DetectedValueMsg;
import distributed.messages.RecordValueMsg;
import distributed.messages.ValueMsg;
import jnr.ffi.annotations.In;

import java.time.Duration;
import java.util.Random;

public class Sensor extends AbstractBehavior<ValueMsg> {

    private String id;
    private int zone;
    private double value;
    private String coordinatorPath;
    private Pair<Integer, Integer> spaceCoords;

    public Sensor(final ActorContext<ValueMsg> context, final String id, final int z, final String cp, final Pair<Integer, Integer> sc) {
        super(context);
        this.id = id;
        this.zone = z;
        this.coordinatorPath = cp;
        this.spaceCoords = sc;
        this.value = -1;
    }

    private void updateValue() {
        this.value = new Random().nextDouble();
    }

    public static Behavior<ValueMsg> create(final String id, final int z, final String cp, final Pair<Integer, Integer> sc) {
        /*
         * Viene creato il sensore specificandogli questo comportamento:
         *   il seguente Behavior una volta impostato è tale da "attivare il sensore" tramite un messaggio
         *   (ValueMsg) che invia ogni N millisecondi
         * */
        return Behaviors.setup(
                context -> {
                    Sensor s = new Sensor(context, id, z, cp, sc);
                    return Behaviors.withTimers(
                            t -> {
                                t.startTimerAtFixedRate(new RecordValueMsg(), Duration.ofMillis(10000));
                                return s;
                            }
                    );
                }
        );
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(RecordValueMsg.class, this::sendData)
                .build();
    }

    /**
     * Quando il Behavior del sensore invia un messaggio al sensore stesso,
     * esso legge/produce un nuovo valore e lo invia al coordinatore della propria zona
     *
     * @param msg - inviato dal Behavior del sensore
     * @return
     */
    private Behavior<ValueMsg> sendData(final RecordValueMsg msg) {
        this.updateValue();
        System.out.println("Sending message from sensor " + id);
        // Example of sending messages
        getContext().classicActorContext()
                .actorSelection(ActorPath.fromString(this.coordinatorPath))
                .tell(new DetectedValueMsg(zone, id, value, this.spaceCoords), ActorRef.noSender());

        return Behaviors.same();
    }

    @Override
    public String toString() {
        return "Sensor{" +
                "id='" + id + '\'' +
                ", zone=" + zone +
                ", value=" + value +
                ", coordinatorPath='" + coordinatorPath + '\'' +
                ", spaceCoords=" + spaceCoords +
                '}';
    }
}
