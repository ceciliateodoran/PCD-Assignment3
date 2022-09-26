package distributed.model;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import distributed.messages.DetectedValueMsg;
import distributed.messages.SnapshotMsg;
import distributed.messages.ValueMsg;
import distributed.messages.ZoneStatus;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;

public class CoordinatorZone extends AbstractBehavior<ValueMsg> {
    private String id;
    private String barrackAddress;
    private int zone;
    private List<SensorSnapshot> sensorSnapshots;

    private CoordinatorZone(final ActorContext<ValueMsg> context, final String id, final String barrackAddress, final int z) {
        super(context);
        this.id = id;
        this.barrackAddress = barrackAddress;
        this.zone = z;
        this.sensorSnapshots = new LinkedList<>();
    }

    public static Behavior<ValueMsg> create(final String id, final String barrackAddress, final int z) {
        return Behaviors.setup(ctx ->{
            CoordinatorZone coordinator = new CoordinatorZone(ctx, id, barrackAddress, z);
            return Behaviors.withTimers(t -> {
               t.startTimerAtFixedRate(new SnapshotMsg(), Duration.ofMillis(8000));
               return coordinator;
            });
        });
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(DetectedValueMsg.class, this::evaluateData)
                .onMessage(SnapshotMsg.class, this::onMessageSnapshot)
                .build();
    }

    private Behavior<ValueMsg> evaluateData(final DetectedValueMsg msg) {
        SensorSnapshot snapshot = new SensorSnapshot(msg.getSensorCoords(), msg.getWaterLevel(), msg.getLimit(), msg.getSensorID(), msg.getDateTimeStamp());
        Logger log = this.getContext().getSystem().log();

        this.sensorSnapshots.removeIf(s -> s.getId() == msg.getSensorID());
        this.sensorSnapshots.add(snapshot);

        log.info("Message received from Coordinator" + this.zone + " : " + msg);

        return Behaviors.same();
    }

    private Behavior<ValueMsg> onMessageSnapshot(final ValueMsg msg) {
        // Logger log = this.getContext().getSystem().log();

        getContext().classicActorContext()
                .actorSelection(ActorPath.fromString(this.barrackAddress))
                .tell(new ZoneStatus("OK", this.sensorSnapshots), ActorRef.noSender());

        // log.info("Sending message to Barrack from Coordinator" + zone);
        System.out.println("Sending message to Barrack from Coordinator" + zone);

        return Behaviors.same();
    }
}
