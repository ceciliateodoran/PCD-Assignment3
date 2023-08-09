package distributed.model;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import distributed.messages.*;
import distributed.messages.selftriggers.ListingResponse;
import distributed.messages.selftriggers.NewIterationMsg;
import distributed.messages.RequestSensorDataMsg;
import distributed.messages.selftriggers.TriggerSendToBarrack;
import distributed.messages.statuses.SensorStatus;
import distributed.messages.statuses.ZoneStatus;
import distributed.model.utility.ExpectedListingResponse;
import distributed.model.utility.IdGenerator;
import distributed.model.utility.SensorSnapshot;

import java.util.*;

import java.time.Duration;

/**
 * Represents the Coordinator zone actor implementation
 */
public class CoordinatorZone extends AbstractBehavior<ValueMsg> {
    private final String id;
    private final List<SensorSnapshot> sensorSnapshots;
    private String seqNumber;
    private final int numSensors;
    private final ActorRef<Receptionist.Listing> listingResponseAdapter;
    private static final IdGenerator idGenerator = new IdGenerator();
    private boolean partialData;
    private final Set<ActorRef<ValueMsg>> reachedSensors;
    private ExpectedListingResponse expectedListingResponse;
    private String status;
    private final Integer zoneNumber;

    private CoordinatorZone(final ActorContext<ValueMsg> context, final String id, final int zoneNumber, final int nSensors) {
        super(context);
        this.id = id;
        this.sensorSnapshots = new ArrayList<>();
        this.numSensors = nSensors;
        this.listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);
        this.partialData = true;
        this.reachedSensors = new HashSet<>();
        this.expectedListingResponse = ExpectedListingResponse.SENSORS;
        this.status = "";
        this.zoneNumber = zoneNumber;
    }

    /**
     * Construct a new instance of the Coordinator zone actor
     *
     * @param id The coordinator zone identifier
     * @param zoneNumber The number of the zone to which the coordinator zone belongs
     * @param numSensors The number of sensors in the zone
     * @return The newly created instance of the Barrack actor
     */
    public static Behavior<ValueMsg> create(final String id, final int zoneNumber, final int numSensors) {
        return Behaviors.setup(context -> {
            //subscribe to receptionist
            context.getSystem()
                    .receptionist()
                    .tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGenerator.getPingKey()), context.getSelf()));
            CoordinatorZone coordinator = new CoordinatorZone(context, id, zoneNumber, numSensors);
            return Behaviors.withTimers(t -> {
                t.startTimerAtFixedRate(new NewIterationMsg(), Duration.ofMillis(5000));
                return coordinator;
            });
        });
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ListingResponse.class, this::onListing)
                .onMessage(SensorStatus.class, this::evaluateData)
                .onMessage(NewIterationMsg.class, this::executeNewIteration)
                .onMessage(TriggerSendToBarrack.class, this::sendStatusAndReset)
                .build();
    }

    private Behavior<ValueMsg> executeNewIteration(NewIterationMsg msg) {
        if(this.expectedListingResponse != ExpectedListingResponse.SENSORS) return Behaviors.same();
        this.sensorSnapshots.clear();
        this.seqNumber = String.valueOf(new Random().nextInt());
        this.getContext()
                .getSystem()
                .receptionist()
                .tell(Receptionist.find(ServiceKey.create(ValueMsg.class, idGenerator.getSensorsKey(this.zoneNumber)), this.listingResponseAdapter));
        return Behaviors.same();
    }
    private Behavior<ValueMsg> sendStatusAndReset(final TriggerSendToBarrack msg) {
        long overflownSensorNumber = this.sensorSnapshots.stream().filter(ss -> ss.getValue() > ss.getLimit()).count();
        this.status = overflownSensorNumber > (numSensors / 2) ? "FLOOD" : "OK";
        System.out.println("zone " + this.zoneNumber + " status: " + status + this.sensorSnapshots.size());

        //send message to barrack
        this.expectedListingResponse = ExpectedListingResponse.BARRACKS;
        this.getContext()
                .getSystem()
                .receptionist()
                .tell(Receptionist.find(ServiceKey.create(ValueMsg.class, idGenerator.getBarrackId(this.zoneNumber)), this.listingResponseAdapter));
        return Behaviors.same();
    }

    private Behavior<ValueMsg> evaluateData(final SensorStatus msg) {
        if (msg.getSeqNumber().equals(this.seqNumber)) {
            SensorSnapshot snapshot = new SensorSnapshot(msg.getSensorCoords(), msg.getWaterLevel(), msg.getLimit(), msg.getSensorID(), msg.getDateTimeStamp());
            this.sensorSnapshots.add(snapshot);
        }
        if (sensorSnapshots.size() == this.reachedSensors.size()) {
            System.out.println("all sensors received of zone " + this.zoneNumber);
            this.getContext().getSelf().tell(new TriggerSendToBarrack());
        }
        return Behaviors.same();
    }

    private Behavior<ValueMsg> onListing(ListingResponse msg) {
        switch (this.expectedListingResponse) {
            case SENSORS -> {
                this.reachedSensors.clear();
                this.reachedSensors.addAll(msg.listing.getServiceInstances(ServiceKey.create(ValueMsg.class, idGenerator.getSensorsKey(this.zoneNumber))));
                this.partialData = this.reachedSensors.size() != this.numSensors;
                this.reachedSensors.forEach(sensor -> sensor.tell(new RequestSensorDataMsg(this.seqNumber, this.getContext().getSelf())));
                break;
            }
            case BARRACKS -> {
                //send status to barracks then reset and ask new status to sensors
                msg.listing.getServiceInstances(ServiceKey.create(ValueMsg.class, idGenerator.getBarrackKey(this.zoneNumber)))
                        .forEach(b -> b.tell(new ZoneStatus(zoneNumber, status, sensorSnapshots, partialData)));
                this.expectedListingResponse = ExpectedListingResponse.SENSORS;
                break;
            }
        }
        return Behaviors.same();
    }
}
