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
import distributed.messages.barrack.commands.ClearBarrack;
import distributed.messages.barrack.commands.CommitBarrack;
import distributed.messages.barrack.commands.DesilenceBarrack;
import distributed.messages.barrack.commands.SilenceBarrack;
import distributed.messages.selftriggers.ListingResponse;
import distributed.messages.selftriggers.UpdateSelfStatusMsg;
import distributed.messages.statuses.BarrackStatus;
import distributed.messages.statuses.CityStatus;
import distributed.messages.statuses.ZoneStatus;
import distributed.model.utility.ExpectedListingResponse;
import distributed.model.utility.IdGenerator;
import distributed.model.utility.SensorSnapshot;
import distributed.utils.Pair;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Represents the Barrack actor implementation
 */
public class Barrack extends AbstractBehavior<ValueMsg> {

    private static final IdGenerator idGenerator = new IdGenerator();
    private String status;
    private Boolean isSilenced;
    private final Map<Integer, Pair<List<SensorSnapshot>, Boolean>> city;
    private final Map<Integer, String> barracks;
    private final ActorRef<Receptionist.Listing> listingResponseAdapter;
    private ExpectedListingResponse expectedListingResponse;
    private final Integer zoneNumber;

    private Barrack(final ActorContext<ValueMsg> context, final int zoneNumber) {
        super(context);
        this.isSilenced = false;
        this.status = "OK";
        this.listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);
        this.city = new HashMap<>();
        this.city.put(zoneNumber, new Pair<>(new ArrayList<>(), true));
        this.barracks = new HashMap<>();
        this.expectedListingResponse = ExpectedListingResponse.BARRACKS;
        this.zoneNumber = zoneNumber;
    }

    /**
     * Construct a new instance of the Barrack actor
     *
     * @param zoneNumber The number of the zone to which the barracks belongs
     * @return The newly created instance of the Barrack actor
     */
    public static Behavior<ValueMsg> create(final int zoneNumber) {
        return Behaviors.setup(context -> {
            //subscribe to receptionist
            context.getSystem()
                    .receptionist()
                    .tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGenerator.getBarracksKey()), context.getSelf()));

            context.getSystem()
                    .receptionist()
                    .tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGenerator.getBarrackKey(zoneNumber)), context.getSelf()));

            context.getSystem()
                    .receptionist()
                    .tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGenerator.getPingKey()), context.getSelf()));

            Barrack barrackActor = new Barrack(context, zoneNumber);
            return Behaviors.withTimers(
                    t -> {
                        t.startTimerAtFixedRate(new UpdateSelfStatusMsg(), Duration.ofMillis(2000));
                        return barrackActor;
                    }
            );
        });
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(UpdateSelfStatusMsg.class, this::sendStatus)
                .onMessage(ZoneStatus.class, this::evaluateZoneData)
                .onMessage(SilenceBarrack.class, this::silenceBarrack)
                .onMessage(DesilenceBarrack.class, this::desilenceBarrack)
                .onMessage(ListingResponse.class, this::onListing)
                .onMessage(BarrackStatus.class, this::updateOtherBarracks)
                .onMessage(CommitBarrack.class, this::commitBarrack)
                .onMessage(ClearBarrack.class, this::clearBarrack)
                .build();
    }

    private Behavior<ValueMsg> updateOtherBarracks(final BarrackStatus msg){
        if(msg.getZone() != this.zoneNumber){
            barracks.put(msg.getZone(), msg.getStatus());
            this.city.put(msg.getZone(), msg.getSensorValues());
        }
        return Behaviors.same();
    }

    private Behavior<ValueMsg> sendStatus(final UpdateSelfStatusMsg msg){
        // #publish
        if(this.expectedListingResponse == ExpectedListingResponse.BARRACKS) {
            this.getContext()
                    .getSystem()
                    .receptionist()
                    .tell(Receptionist.find(ServiceKey.create(ValueMsg.class, idGenerator.getBarracksKey()), this.listingResponseAdapter));
        }

        return Behaviors.same();
    }

    private Behavior<ValueMsg> evaluateZoneData(final ZoneStatus msg) {
        if(msg.getZone() == this.zoneNumber){
            if(this.status.equals("OK") && !this.isSilenced && msg.getStatus().equals("FLOOD")){
                this.status = "FLOOD";
            }
            List<SensorSnapshot> ssList = this.city.get(msg.getZone()).first();
            ssList.clear();
            ssList.addAll(msg.getSnapshot());
            this.city.put(msg.getZone(), new Pair<>(ssList, msg.getPartialData()));
        }
        this.barracks.put(this.zoneNumber, this.status);
        return  Behaviors.same();
    }

    private Behavior<ValueMsg> silenceBarrack(final SilenceBarrack msg) {
        if(!this.isSilenced) this.status = "SILENCED";
        this.isSilenced = true;
        return  Behaviors.same();
    }

    private Behavior<ValueMsg> desilenceBarrack(final DesilenceBarrack msg) {
        if(this.isSilenced) this.status = "OK";
        this.isSilenced = false;
        return  Behaviors.same();
    }

    private Behavior<ValueMsg> commitBarrack(final CommitBarrack msg) {
        if(this.status.equals("FLOOD")) this.status = "COMMITTED";
        return  Behaviors.same();
    }

    private Behavior<ValueMsg> clearBarrack(final ClearBarrack msg) {
        if(this.status.equals("COMMITTED")) this.status = "OK";
        return  Behaviors.same();
    }

    private Behavior<ValueMsg> onListing(ListingResponse msg) {
        if(msg.listing.getKey().id().equals("barracks")) {
            if (!this.city.get(this.zoneNumber).first().isEmpty()) {
                msg.listing.getServiceInstances(ServiceKey.create(ValueMsg.class, idGenerator.getBarracksKey()))
                        .forEach(b -> b.tell(new BarrackStatus(status, ZonedDateTime.now(), zoneNumber, this.city.get(this.zoneNumber))));
            }
            this.expectedListingResponse = ExpectedListingResponse.GUIS;
            this.getContext()
                    .getSystem()
                    .receptionist()
                    .tell(Receptionist.find(ServiceKey.create(ValueMsg.class, idGenerator.getGuisKey(this.zoneNumber)), this.listingResponseAdapter));
        } else {
            //send to your guis the city status and your barrack status
            msg.listing.getServiceInstances(ServiceKey.create(ValueMsg.class, idGenerator.getGuisKey(this.zoneNumber)))
                    .forEach(gui -> {
                        if (!city.isEmpty()) {
                            gui.tell(new CityStatus(city, barracks));
                        }
                    });
            this.expectedListingResponse = ExpectedListingResponse.BARRACKS;
        }

        return Behaviors.same();
    }
}
