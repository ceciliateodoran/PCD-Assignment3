package actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

/**
 * attore che crea BodyActor e ViewActor e che si occupa
 * sia di inviare i nuovi dati calcolati dal BodyActor al ViewActor,
 * sia di segnalare al BodyActor l'inizio e la terminazione del processo di calcolo
 */
public class ControllerActor extends AbstractBehavior<ControllerMsg> {
    private static int totBodies;

    private static int maxIter;

    private static int viewHeight;

    private static int viewWidth;

    private int currentIter;

    private double vt;

    /* virtual time step */
    private final double dt;

    private ActorRef<BodyMsg> bodyActorRef;

    private ActorRef<ViewMsg> viewActorRef;

    private ControllerActor(final ActorContext<ControllerMsg> context) {
        super(context);
        this.dt = 0.001;
        resetCounters();
        createActors(context);
    }

    private void createActors(final ActorContext<ControllerMsg> context) {
        this.bodyActorRef = context.spawn(BodyActor.create(context.getSelf(), totBodies), "bodyActor");

        // invio del messaggio di start al BodyActor (versione NO GUI)
        // this.bodyActorRef.tell(new ComputePositionsMsg(context.getSelf(), this.dt));

        // creazione della GUI
        this.viewActorRef = context.spawn(ViewActor.create(context.getSelf(), viewWidth, viewHeight), "viewActor");
    }

    @Override
    public Receive<ControllerMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(UpdatedPositionsMsg.class, this::onUpdatePos)
                .onMessage(ViewStopMsg.class, this::onStop)
                .onMessage(ViewUpdatedMsg.class, this::onViewUpdated)
                .onMessage(ViewStartMsg.class, this::onViewStart)
                .build();
    }

    /* messaggio ricevuto dal ViewActor quando viene catturato l'evento di pressione del bottone Start */
    private Behavior<ControllerMsg> onViewStart(final ViewStartMsg msg) {
        this.bodyActorRef.tell(new ComputePositionsMsg(this.getContext().getSelf(), this.dt));
        return this;
    }

    /* messaggio ricevuto dal BodyActor quando sono stati calcolati i nuovi valori di velocità e posizione dei Body */
    private Behavior<ControllerMsg> onUpdatePos(final UpdatedPositionsMsg msg) {
        //this.getContext().getLog().info("ControllerActor: message of start pos calculation received.");

        if (this.currentIter < maxIter) {
            this.vt += this.dt;
            this.currentIter++;

            /* GUI version */
            this.viewActorRef.tell(new UpdatedPositionsMsg(msg.getBodies(), this.vt, this.currentIter, msg.getBounds()));

            /* No-GUI version */
            /* if (this.currentIter == maxIter) {
                // reset
                resetCounters();
                this.bodyActorRef.tell(new StopMsg());

            } else {
                //ricominciare il calcolo
                this.bodyActorRef.tell(new ComputePositionsMsg(this.getContext().getSelf(), this.dt));
            }*/
        }
        return this;
    }

    /* messaggio ricevuto dal ViewActor all'aggiornamento della GUI */
    private Behavior<ControllerMsg> onViewUpdated(final ViewUpdatedMsg msg) {
        if (this.currentIter == maxIter) {
            // reset
            resetCounters();
            this.bodyActorRef.tell(new StopMsg());

            //inviare msg di fine iterazioni a GUI
            this.viewActorRef.tell(new ControllerStopMsg());
        } else {
            //ricominciare il calcolo
            this.bodyActorRef.tell(new ComputePositionsMsg(this.getContext().getSelf(), this.dt));
        }
        return this;
    }

    /* messaggio ricevuto dal ViewActor quando viene catturato l'evento di pressione del bottone Stop */
    private Behavior<ControllerMsg> onStop(final ViewStopMsg msg) {
        //this.getContext().getLog().info("ControllerActor: stop message received from GUI.");

        // reset dei bodies
        this.bodyActorRef.tell(new StopMsg());

        return this;
    }

    /* public factory to create Controller actor */
    public static Behavior<ControllerMsg> create(final int bodies, final int iter, final int w, final int h) {
        totBodies = bodies;
        maxIter = iter;
        viewHeight = h;
        viewWidth = w;
        return Behaviors.setup(ControllerActor::new);
    }

    private void resetCounters() {
        this.currentIter = 0;
        this.vt = 0;
    }
}
