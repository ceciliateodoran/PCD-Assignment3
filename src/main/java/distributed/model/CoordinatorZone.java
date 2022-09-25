package distributed.model;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.japi.Pair;
import distributed.messages.DetectedValueMsg;
import distributed.messages.ValueMsg;
import org.slf4j.Logger;

import java.util.LinkedList;
import java.util.List;

public class CoordinatorZone extends AbstractBehavior<ValueMsg> {
    private String id;
    private int zone;
    private List<Pair<Integer, Integer>> sensorList;

    private CoordinatorZone(final ActorContext<ValueMsg> context, final String id, final int z) {
        super(context);
        this.id = id;
        this.zone = z;
        this.sensorList = new LinkedList<>();
    }

    public static Behavior<ValueMsg> create(final String id, final int z) {
        return Behaviors.setup(ctx -> new CoordinatorZone(ctx, id, z));
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(DetectedValueMsg.class, this::evaluateData)
                .build();
    }

    private Behavior<ValueMsg> evaluateData(final DetectedValueMsg msg) {
        Logger log = this.getContext().getSystem().log();

        if (!this.sensorList.contains(msg.getSensorCoords()))
            this.sensorList.add(msg.getSensorCoords());

        log.info("Message received from Coordinator" + this.zone + " : " + msg);

        return Behaviors.same();
    }
}
