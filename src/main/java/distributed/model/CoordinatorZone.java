package distributed.model;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.pubsub.Topic;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import distributed.messages.*;

import java.util.*;

import java.time.Duration;

public class CoordinatorZone extends AbstractBehavior<ValueMsg> {
    private final String id;
    private final int zone;
    private final List<SensorSnapshot> sensorSnapshots;
    private String seqNumber;
    private final int numSensors;
    private boolean firstIterationFlag;
    private final ActorRef<Receptionist.Listing> listingResponseAdapter;
    private static final IdGenerator idGenerator = new IdGenerator();
    private boolean partialData;
    private Set<ActorRef<ValueMsg>> reachedSensors;
    private ExpectedListingResponse expectedListingResponse;
    private String status;

    private CoordinatorZone(final ActorContext<ValueMsg> context, final String id, final int z, final int nSensors) {
        super(context);
        this.id = id;
        this.zone = z;
        this.sensorSnapshots = new ArrayList<>();
        this.numSensors = nSensors;
        this.firstIterationFlag = true;
        this.listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);
        this.partialData = true;
        this.reachedSensors = new HashSet<>();
        this.expectedListingResponse = ExpectedListingResponse.SENSORS;
        this.status = "";
    }

    public static Behavior<ValueMsg> create(final String id, final int z, final int numSensors) {
        return Behaviors.setup(context -> {
            CoordinatorZone coordinator = new CoordinatorZone(context, id, z, numSensors);
            context.getSystem()
                    .receptionist()
                    .tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGenerator.getZoneId(z)), context.getSelf()));
            return Behaviors.withTimers(t -> {
                t.startTimerAtFixedRate(new FirstIterationMsg(), Duration.ofMillis(8000));
                return coordinator;
            });
        });
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ListingResponse.class, this::onListing)
                .onMessage(DetectedValueMsg.class, this::evaluateData)
                .onMessage(FirstIterationMsg.class, this::executeFirstIteration)
                .onMessage(TriggerSendToBarrack.class, this::sendStatusAndReset)
                .build();
    }

    private Behavior<ValueMsg> executeFirstIteration(FirstIterationMsg msg) {
        if(firstIterationFlag){
            this.firstIterationFlag = false;
            this.seqNumber = String.valueOf(new Random().nextInt());
            this.getContext()
                    .getSystem()
                    .receptionist()
                    .tell(Receptionist.find(ServiceKey.create(ValueMsg.class, idGenerator.getZoneId(zone)+"-sensors"), this.listingResponseAdapter));
        }
        return Behaviors.same();
    }
    private Behavior<ValueMsg> sendStatusAndReset(final TriggerSendToBarrack msg) {
        long overflownSensorNumber = this.sensorSnapshots.stream().filter(ss -> ss.getValue() > ss.getLimit()).count();
        this.status = overflownSensorNumber > (sensorSnapshots.size() / 2) ? "FLOOD" : "OK";
        System.out.println("zone " + this.zone + " status: " + status + this.sensorSnapshots.size());

        //send message to barrack
        this.expectedListingResponse = ExpectedListingResponse.BARRACKS;
        this.getContext()
                .getSystem()
                .receptionist()
                .tell(Receptionist.find(ServiceKey.create(ValueMsg.class, "barracks"), this.listingResponseAdapter));
        return Behaviors.same();
    }

    private Behavior<ValueMsg> evaluateData(final DetectedValueMsg msg) {
        if (msg.getSeqNumber().equals(this.seqNumber)) {
            SensorSnapshot snapshot = new SensorSnapshot(msg.getSensorCoords(), msg.getWaterLevel(), msg.getLimit(), msg.getSensorID(), msg.getDateTimeStamp());
            this.sensorSnapshots.add(snapshot);
        }
        System.out.println("sensorSnapshots size: "+this.sensorSnapshots.size() + " reachedSensors size: "+this.reachedSensors.size());
        if (sensorSnapshots.size() == this.reachedSensors.size()) {
            System.out.println("all sensors received of zone " + this.zone);
            this.getContext().getSelf().tell(new TriggerSendToBarrack());
        }
        return Behaviors.same();
    }

    private Behavior<ValueMsg> onListing(ListingResponse msg) {
        switch (this.expectedListingResponse){
            case SENSORS: {
                this.reachedSensors.clear();
                this.reachedSensors.addAll(msg.listing.getServiceInstances(ServiceKey.create(ValueMsg.class, idGenerator.getZoneId(zone)+"-sensors")));
                this.partialData = this.reachedSensors.size() != this.numSensors;
                this.reachedSensors.forEach(sensor -> sensor.tell(new RequestSensorDataMsg(this.seqNumber, this.getContext().getSelf())));
                break;
            }

            case BARRACKS: {
                //send status to barracks then reset and ask new status to sensors
                msg.listing.getServiceInstances(ServiceKey.create(ValueMsg.class, "barracks"))
                        .forEach(b -> b.tell(new ZoneStatus(zone, status, sensorSnapshots, partialData)));
                this.sensorSnapshots.clear();
                seqNumber = String.valueOf(new Random().nextInt());
                this.expectedListingResponse = ExpectedListingResponse.SENSORS;
                this.getContext()
                        .getSystem()
                        .receptionist()
                        .tell(Receptionist.find(ServiceKey.create(ValueMsg.class, idGenerator.getZoneId(zone)+"-sensors"), this.listingResponseAdapter));
                break;
            }
        }

        return Behaviors.same();
    }
}
