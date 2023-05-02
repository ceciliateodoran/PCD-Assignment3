package distributed.model;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import akka.japi.Pair;
import distributed.messages.*;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;

public class Barrack extends AbstractBehavior<ValueMsg> {

    private int zone;
    private static IdGenerator idGenerator = new IdGenerator();
    private String status;
    private Boolean isSilenced;
    private final Map<Integer, Pair<List<SensorSnapshot>, Boolean>> city;
    private final Map<Integer, String> barracks;
    private final ActorRef<Receptionist.Listing> listingResponseAdapter;
    private ExpectedListingResponse expectedListingResponse;

    private Barrack(final ActorContext<ValueMsg> context, final int z) {
        super(context);
        this.zone = z;
        this.isSilenced = false;
        this.status = "OK";
        this.listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);
        this.city = new HashMap<>();
        this.barracks = new HashMap<>();
        this.expectedListingResponse = ExpectedListingResponse.BARRACKS;
    }

    //COMPORTAMENTO: manda a te stesso un messaggio ogni tot millisecondi per ricordarti di mandare il tuo stato alla GUI

    public static Behavior<ValueMsg> create(final int z) {
        return Behaviors.setup(context -> {
            //TODO remove subscription once we tested
            //subscribe to receptionist
            context.getSystem()
                    .receptionist()
                    .tell(Receptionist.register(ServiceKey.create(ValueMsg.class, "barracks"), context.getSelf()));
            /*
             * Viene creata la caserma specificandogli questo comportamento:
             *   il seguente Behavior una volta impostato è tale da "attivare la caserma " tramite un messaggio
             *   (ValueMsg) che invia ogni N millisecondi
             * */

            Barrack b = new Barrack(context, z);
            return Behaviors.withTimers(
                t -> {
                    t.startTimerAtFixedRate(new UpdateSelfStatusMsg(), Duration.ofMillis(10000));
                    return b;
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
                .onMessage(ListingResponse.class, this::onListing)
                .onMessage(BarrackStatus.class, this::updateOtherBarracks)
                .build();
    }

    private Behavior<ValueMsg> updateOtherBarracks(final BarrackStatus msg){
        if(msg.getZone() != this.zone){
            barracks.put(msg.getZone(), msg.getStatus());
        }
        return Behaviors.same();
    }
    private Behavior<ValueMsg> sendStatus(final UpdateSelfStatusMsg msg){
        System.out.println("Sending message from barrack of zone " + zone);
        // #publish
        this.getContext()
                .getSystem()
                .receptionist()
                .tell(Receptionist.find(ServiceKey.create(ValueMsg.class, "barracks"), this.listingResponseAdapter));
        return Behaviors.same();
    }

    //COMPORTAMENTO: valuta i dati ricevuti dal controllore di zona
    private Behavior<ValueMsg> evaluateZoneData(final ZoneStatus msg) {
        if(msg.getZone() == zone){
            Logger log = this.getContext().getSystem().log();
            log.info("Message received from Coordinator" + zone + " : " + msg.toString());
            if(this.status.equals("OK") && !this.isSilenced && msg.getStatus().equals("CRYSIS")){
                this.status = "CRYSIS";
                this.barracks.put(this.zone, this.status);
            }
        }
        this.city.put(msg.getZone(), Pair.create(msg.getSnapshot(), msg.getPartialData()));
        return  Behaviors.same();
    }

    //COMPORTAMENTO: silenzia o desilenzia la caserma in base ai messaggi dalla GUI
    private Behavior<ValueMsg> silenceBarrack(final SilenceBarrack msg) {
        if(!this.isSilenced) this.status = "OK"; //se non era silenziata vuol dire che ora sta venendo silenziata, quindi imposta lo stato a OK ignorando tutto
        this.isSilenced = !this.isSilenced;
        return  Behaviors.same();
    }

    //COMPORTAMENTO: committa la caserma alla gestione dell'allarme, passa dallo stato CRYSIS allo stato COMMITTED
    private Behavior<ValueMsg> commitBarrack(final SilenceBarrack msg) {
        if(this.status.equals("CRYSIS")) this.status = "COMMITTED";
        return  Behaviors.same();
    }

    //COMPORTAMENTO: allarme gestito, torna allo stato OK
    private Behavior<ValueMsg> clearBarrack(final SilenceBarrack msg) {
        if(this.status.equals("COMMITTED")) this.status = "OK";
        return  Behaviors.same();
    }

    private Behavior<ValueMsg> onListing(ListingResponse msg) {
        switch(this.expectedListingResponse){
            case BARRACKS:
                //send status to barracks then reset and ask new status to sensors
                msg.listing.getServiceInstances(ServiceKey.create(ValueMsg.class, "barracks"))
                        .forEach(b -> b.tell(new BarrackStatus(status, ZonedDateTime.now(), zone)));
                this.expectedListingResponse = ExpectedListingResponse.GUIS;
                this.getContext()
                        .getSystem()
                        .receptionist()
                        .tell(Receptionist.find(ServiceKey.create(ValueMsg.class, "gui:"+zone), this.listingResponseAdapter));
                break;

            case GUIS:
                //send to your guis the city status and your barrack status
                msg.listing.getServiceInstances(ServiceKey.create(ValueMsg.class, "gui:"+zone))
                        .forEach(gui -> {
                            gui.tell(new CityStatus(city));
                            gui.tell(new BarrackStatus(status, ZonedDateTime.now(), zone));
                        });
                this.expectedListingResponse = ExpectedListingResponse.BARRACKS;
        }
        return Behaviors.same();
    }
}
