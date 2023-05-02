package distributed.model;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import akka.actor.typed.pubsub.Topic;
import distributed.messages.*;
import org.slf4j.Logger;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class Barrack extends AbstractBehavior<ValueMsg> {

    private static int zone;
    private String status;
    private Boolean isSilenced;
    private List<SensorSnapshot> sensors;
    private  static ActorRef<Topic.Command<BarrackStatus>> topic;

    private Barrack(final ActorContext<ValueMsg> context, final int z) {
        super(context);
        zone = z;
        this.isSilenced = false;
        this.status = "OK";
        this.sensors = new ArrayList<>();
    }

    //COMPORTAMENTO: manda a te stesso un messaggio ogni tot millisecondi per ricordarti di mandare il tuo stato alla GUI

    public static Behavior<ValueMsg> create(final int z) {
        return Behaviors.setup(ctx -> {
            topic = ctx.spawn(Topic.create(BarrackStatus.class, "my-topic"), "MyTopic");
            //TODO remove subscription once we tested

            //topic.tell(Topic.subscribe(ctx.getSelf()));

            /*
             * Viene creata la caserma specificandogli questo comportamento:
             *   il seguente Behavior una volta impostato è tale da "attivare la caserma " tramite un messaggio
             *   (ValueMsg) che invia ogni N millisecondi
             * */
            return Behaviors.setup(
                context -> {
                    Barrack b = new Barrack(ctx, z);
                    return Behaviors.withTimers(
                        t -> {
                            t.startTimerAtFixedRate(new UpdateSelfStatusMsg(), Duration.ofMillis(10000));
                            return b;
                        }
                    );
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
                .build();
    }

    private Behavior<ValueMsg> sendStatus(final UpdateSelfStatusMsg msg){
        System.out.println("Sending message from barrack of zone " + zone);
        // #publish
        topic.tell(Topic.publish(new BarrackStatus(status, ZonedDateTime.now(), sensors)));
        return Behaviors.same();
    }

    //COMPORTAMENTO: valuta i dati ricevuti dal controllore di zona
    private Behavior<ValueMsg> evaluateZoneData(final ZoneStatus msg) {
        Logger log = this.getContext().getSystem().log();
        log.info("Message received from Coordinator" + zone + " : " + msg.toString());
        if(this.status.equals("OK") && !this.isSilenced && msg.getStatus().equals("CRYSIS")){
            this.status = "CRYSIS";
        }
        this.sensors = msg.getSnapshot();
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
}
