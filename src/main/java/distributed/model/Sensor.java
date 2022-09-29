package distributed.model;


import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.*;
import akka.actor.typed.pubsub.Topic;
import akka.japi.Pair;
import distributed.messages.*;
import jnr.ffi.annotations.In;

import java.util.Random;

public class Sensor extends AbstractBehavior<ValueMsg> {
    private String id;
    private int zone;
    private double value;
    private double limit;
    private String coordinatorPath;
    private Pair<Integer, Integer> spaceCoords;
    private akka.actor.typed.ActorRef<Topic.Command<ValueMsg>> topic;

    public Sensor(final ActorContext<ValueMsg> context, final String id, final int z, final String cp, final Pair<Integer, Integer> sc) {
        super(context);
        this.id = id;
        this.zone = z;
        this.coordinatorPath = cp;
        this.spaceCoords = sc;
        this.limit = new Random().nextDouble();
        this.value = -1;
        this.topic = context.spawn(Topic.create(ValueMsg.class, "zone-"+zone+"-channel"), "zone-"+zone+"-topic");
        this.topic.tell(Topic.subscribe(context.getSelf()));
    }

    private void updateValue() {
        this.value = new Random().nextDouble();
    }

    public static Behavior<ValueMsg> create(final String id, final int z, final String cp, final Pair<Integer, Integer> sc) {
        return Behaviors.setup(context -> new Sensor(context, id, z, cp, sc));
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ValueMsg.class, this::sendData)
                .build();
    }

    /**
     * Quando il Behavior del sensore invia un messaggio al sensore stesso,
     * esso legge/produce un nuovo valore e lo invia al coordinatore della propria zona
     *
     * @param msg - inviato dal Behavior del sensore
     * @return
     */
    private Behavior<ValueMsg> sendData(final ValueMsg msg) {
        this.updateValue();
        System.out.println("Sending message from sensor " + id);

        getContext().classicActorContext()
                .actorSelection(ActorPath.fromString(this.coordinatorPath))
                .tell(new DetectedValueMsg(zone, id, value, this.limit, this.spaceCoords, msg.getValue()), ActorRef.noSender());

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
