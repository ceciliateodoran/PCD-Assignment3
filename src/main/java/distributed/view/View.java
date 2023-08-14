package distributed.view;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.receptionist.Receptionist;
import akka.actor.typed.receptionist.ServiceKey;
import distributed.CityZone;
import distributed.messages.barrack.commands.ClearBarrack;
import distributed.messages.barrack.commands.CommitBarrack;
import distributed.messages.barrack.commands.DesilenceBarrack;
import distributed.messages.barrack.commands.SilenceBarrack;
import distributed.messages.ValueMsg;
import distributed.messages.selftriggers.ListingResponse;
import distributed.messages.selftriggers.UpdateSelfStatusMsg;
import distributed.messages.statuses.CityStatus;
import distributed.model.utility.IdGenerator;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Represents the View actor implementation
 */
public class View extends AbstractBehavior<ValueMsg> {

    private static final IdGenerator idGenerator = new IdGenerator();
    private final Server server;
    private final int zone;
    private Map<String, Integer> cityDimensions;
    private List<CityZone> cityZones;
    private static boolean guiChanges;
    private static String statusAlarm;
    private ActorRef<Receptionist.Listing> listingResponseAdapter;

    public View(final ActorContext<ValueMsg> context, final int zone, final Map<String, Integer> cityDimensions, final List<CityZone> cityZones) {
        super(context);
        this.listingResponseAdapter = context.messageAdapter(Receptionist.Listing.class, ListingResponse::new);
        this.zone = zone;
        this.cityDimensions = cityDimensions;
        this.cityZones = cityZones;
        guiChanges = false;
        this.server = new Server(this, this.zone, this.cityDimensions, this.cityZones);
    }

    /**
     * Construct a new instance of the View actor and the Server
     *
     * @param zone The number of the zone to which the view belongs
     * @param cityDimensions The dimensions of the city, such as width, height, rows and columns
     * @param cityZones The list of all the city zones
     * @return The newly created instance of the View actor
     */
    public static Behavior<ValueMsg> create(final int zone, final Map<String, Integer> cityDimensions, final List<CityZone> cityZones) {
        return Behaviors.setup(context -> {
            //subscribe to receptionist
            context.getSystem()
                    .receptionist()
                    .tell(Receptionist.register(ServiceKey.create(ValueMsg.class, idGenerator.getGuisKey(zone)), context.getSelf()));
            View view = new View(context, zone, cityDimensions, cityZones);

            //self msg every t time
            return Behaviors.withTimers(
                    t -> {
                        t.startTimerAtFixedRate(new UpdateSelfStatusMsg(), Duration.ofMillis(2000));
                        return view;
                    }
            );
        });
    }

    @Override
    public Receive<ValueMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(UpdateSelfStatusMsg.class, this::sendStatus)
                .onMessage(ListingResponse.class, this::onListing)
                .onMessage(CityStatus.class, this::onBarrackDataMsg)
                .build();
    }

    // it triggers onListing
    private Behavior<ValueMsg> sendStatus(final UpdateSelfStatusMsg a) {
        this.getContext().getSystem()
                .receptionist()
                .tell(Receptionist.find(ServiceKey.create(ValueMsg.class, idGenerator.getBarrackId(this.zone)), this.listingResponseAdapter));

        return Behaviors.same();
    }

    //if there are actions from gui, send the corresponding message to your barrack
    private Behavior<ValueMsg> onListing(final ListingResponse msg) {
        if (guiChanges){
            msg.listing.getServiceInstances(ServiceKey.create(ValueMsg.class, idGenerator.getBarrackId(this.zone)))
                    .forEach(barrack -> {
                        if(statusAlarm.equals("COMMITTED")){
                            barrack.tell(new CommitBarrack());
                        }else if(statusAlarm.equals("OK")){
                            barrack.tell(new ClearBarrack());
                        }else if(statusAlarm.equals("SILENCED")){
                            barrack.tell(new SilenceBarrack());
                        } else if (statusAlarm.equals("DESILENCED")) {
                            barrack.tell(new DesilenceBarrack());
                        }
                    });
            guiChanges = false;
        }
        return Behaviors.same();
    }

    private Behavior<ValueMsg> onBarrackDataMsg(final CityStatus msg) {
        this.server.setBarracksStatuses(msg.getBarracksStatuses());
        this.server.setSensorStatuses(msg.getSensorStatuses());

        return Behaviors.same();
    }

    /**
     * It changes the status of the barrack, triggered from Server
     * when there are actions on gui from users
     *
     * @param status The new status of the Barrack
     */
    public static void changeBarrackStatus(final String status){
        statusAlarm = status;
        guiChanges = true;
    }
}
