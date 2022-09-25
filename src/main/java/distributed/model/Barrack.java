package distributed.model;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
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
import java.util.HashMap;
import java.util.Map;

public class Barrack extends AbstractBehavior<ValueMsg> {

    private static int zone;
    private String status;
    private Boolean isSilenced;
    private final String GUIAddress;
    private Map<String, Double> floodedSensors;

    private Barrack(final ActorContext<ValueMsg> context, final int z, final String GUIaddress) {
        super(context);
        this.zone = z;
        this.GUIAddress = GUIaddress;
        this.floodedSensors = new HashMap<>();
    }

    //COMPORTAMENTO: manda a te stesso un messaggio ogni tot millisecondi per ricordarti di mandare il tuo stato alla GUI

    public static Behavior<ValueMsg> create(final int z, final String GUIaddress) {
        return Behaviors.setup(ctx -> {
            /*
             * Viene creata la caserma specificandogli questo comportamento:
             *   il seguente Behavior una volta impostato è tale da "attivare la caserma " tramite un messaggio
             *   (ValueMsg) che invia ogni N millisecondi
             * */
            return Behaviors.setup(
                context -> {
                    Barrack b = new Barrack(ctx, z, GUIaddress);
                    return Behaviors.withTimers(
                        t -> {
                            t.startTimerAtFixedRate(new RecordValueMsg(), Duration.ofMillis(10000));
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
                .onMessage(RecordValueMsg.class, this::sendStatus)
                .onMessage(ZoneStatus.class, this::evaluateZoneData)
                .onMessage(SilenceBarrack.class, this::silenceBarrack)
                .build();
    }

    private Behavior<ValueMsg> sendStatus(final RecordValueMsg msg){
        System.out.println("Sending message from barrack of zone " + zone);
        //TODO change to broadcast
        getContext().classicActorContext()
                .actorSelection(ActorPath.fromString(this.GUIAddress))
                .tell(new BarrackStatus(status, ZonedDateTime.now(), floodedSensors), ActorRef.noSender());

        return Behaviors.same();
    }

    //COMPORTAMENTO: valuta i dati ricevuti dal controllore di zona
    private Behavior<ValueMsg> evaluateZoneData(final ZoneStatus msg) {
        Logger log = this.getContext().getSystem().log();
        log.info("Message received from Coordinator" + zone + " : " + msg.toString());
        if(this.status.equals("OK") && !this.isSilenced && msg.getStatus().equals("CRYSIS")){
            this.status = "CRYSIS";
        }
        this.floodedSensors = msg.getSnapshot();
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
