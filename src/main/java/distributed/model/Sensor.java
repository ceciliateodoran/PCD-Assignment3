package distributed.model;


import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import distributed.messages.statuses.SensorStatus;
import distributed.messages.selftriggers.UpdateSelfStatusMsg;
import distributed.messages.RequestSensorDataMsg;
import distributed.messages.ValueMsg;
import distributed.model.utility.IdGenerator;
import distributed.utils.Pair;

import java.time.Duration;
import java.util.Random;

/**
 * Represents the Sensor actor implementation
 */
public class Sensor extends AbstractBehavior<ValueMsg> {
    private String id;
    private double value;
    private final double limit;
    private final Pair<Integer, Integer> sensorCoords;
    private static final IdGenerator idGenerator = new IdGenerator();
    private final Integer zoneNumber;

    private Sensor(final ActorContext<ValueMsg> context, final String id, final int zoneNumber, final Pair<Integer, Integer> sensorCoords, double limit) {
        super(context);
        this.id = id;
        this.sensorCoords = sensorCoords;
        this.limit = limit;
        this.value = -1;
        this.zoneNumber = zoneNumber;
    }

    private void updateValue() {
        Long upperLimit = Math.round(this.limit + this.limit/2);
        this.value = new Random().nextInt(Integer.parseInt(upperLimit.toString()));
    }

    /**
     * Construct a new instance of the Sensor actor
     *
     * @param id The sensor identifier
     * @param zoneNumber The number of the zone to which the sensor belongs
     * @param sensorCoords The space coordinates of the sensor
     * @param limit The maximum limit that the water level can reach
     * @return The newly created instance of the sensor actor
     */
    public static Behavior<ValueMsg> create(final String id, final int zoneNumber, final Pair<Integer, Integer> sensorCoords, double limit) {
        return Behaviors.setup(
                context -> {
                    Sensor s = new Sensor(context, id, zoneNumber, sensorCoords, limit);
                    //subscribe to receptionist
                    context.getSystem()
                            .receptionist()
                            .tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGenerator.getPingKey()), context.getSelf()));

                    context.getSystem()
                            .receptionist()
                            .tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGenerator.getSensorsKey(zoneNumber)), context.getSelf()));
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
        msg.getReplyTo().tell(new SensorStatus(zoneNumber, id, value, this.limit, this.sensorCoords, msg.getSeqNumber()));
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
                ", zone=" + zoneNumber +
                ", value=" + value +
                ", limit=" + limit +
                ", spaceCoords=" + sensorCoords +
                '}';
    }
}
