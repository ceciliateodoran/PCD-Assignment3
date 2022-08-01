package actor;

import actor.utils.Body;
import actor.utils.BodyGenerator;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;

import java.util.List;

public class BodyActor extends AbstractBehavior<BodyMsg> {
    private static int nBodies;

    private Boundary bounds;

    private List<Body> bodies;

    private static ActorRef<ControllerMsg> controllerActorRef;

    public BodyActor(final ActorContext<BodyMsg> context) {
        super(context);
        initializeBodies(nBodies);
    }

    @Override
    public Receive<BodyMsg> createReceive() {
        return newReceiveBuilder()
                .onMessage(ComputePositionMsg.class, this::onNewIteration)
                .onMessage(UpdatePositionMsg.class, this::onNewPosition)
                .onMessage(StopMsg.class, this::onStop)
                .build();
    }

    /* manda messaggio a VelocityCalculator per iniziare a calcolare i nuovi valori */
    private Behavior<BodyMsg> onNewIteration(final ComputePositionMsg msg) {
        //this.getContext().getLog().info("BodyActor: position's computation message received from ControllerActor.");
        // Invio ogni body al VelocityCalculatorActor
        for (int i = 0; i < this.bodies.size(); i++) {
            // System.out.println("onNewIteration -> iteration n." + i);    // debug
            msg.getVelCalcActorRef().tell(new ComputeVelocityMsg(this.getContext().getSelf(), msg.getPosCalcActorRef(),
                    this.bodies.get(i), this.bodies, this.bounds, msg.getDt()));
        }
        return this;
    }

    /* aggiorna i valori delle nuove posizioni calcolate dal PositionCalculator e manda
    * i risultati al ControllerActor */
    private Behavior<BodyMsg> onNewPosition(final UpdatePositionMsg msg) {
        //this.getContext().getLog().info("BodyActor: updated position message received from PositionCalculatorActor.");
        // aggiorno la posizione dell'i-esimo body
        this.bodies.set(msg.getBodyIndex(), msg.getUpdatedBody());
        // mando il valore della posizione al ControllerActor
        this.controllerActorRef.tell(new PositionsMsg(this.getContext().getSelf(), this.bodies, msg.getDt(), this.bounds));
        return this;
    }

    private Behavior<BodyMsg> onStop(final StopMsg msg) {
        //this.getContext().getLog().info("BodyActor: iterations stop message received from ControllerActor.");
        initializeBodies(nBodies); // resetto i bodies a dei nuovi valori (per un eventuale re-start)

        return this; // Behaviors.stopped();
    }

    /* public factory to create the actor */
    public static Behavior<BodyMsg> create(final ActorRef<ControllerMsg> ctrlerActor, final int totBodies) {
        controllerActorRef = ctrlerActor;
        nBodies = totBodies;
        return Behaviors.setup(BodyActor::new);
    }

    private void initializeBodies(final int totBodies) {
        this.bounds =  new Boundary(-6.0, -6.0, 6.0, 6.0);
        BodyGenerator bg = new BodyGenerator();
        this.bodies = bg.generateBodies(totBodies, this.bounds);
    }
}
