package actor;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import actor.utils.Body;
import actor.utils.BodyGenerator;
import actor.utils.Boundary;

import java.util.ArrayList;

public class BodyActor extends AbstractBehavior<BodyMsg> {
    private static int nBodies;

    private ArrayList<Body> bodies;

    public BodyActor(ActorContext<BodyMsg> context) {
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
    private Behavior<BodyMsg> onNewIteration(ComputePositionMsg msg) {
        // Invio ogni body al VelocityCalculatorActor
        for (int i = 0; i < bodies.size(); i++) {
            msg.getReplyTo().tell(new ComputeVelocityMsg(this.getContext().getSelf(), this.bodies.get(i), i));
        }
        return this;
    }

    /* aggiorna i valori delle nuove posizioni calcolate dal PositionCalculator e manda
    * i risultati al ControllerActor */
    private Behavior<BodyMsg> onNewPosition(UpdatePositionMsg msg) {
        // aggiorno la posizione dell'i-esimo body
        this.bodies.set(msg.getBodyIndex(), msg.getUpdatedBody());
        // mando il valore della posizione al ControllerActor
        msg.getReplyTo().tell(new PositionsMsg(this.getContext().getSelf(), msg.getUpdatedBody().getPos()));
        return this;
    }

    private Behavior<BodyMsg> onStop(StopMsg msg) {
        this.getContext().getLog().info("stopMsg");
        initializeBodies(nBodies); // resetto i bodies a dei nuovi valori (per un eventuale re-start)
        return this; // Behaviors.stopped();
    }

    /* public factory to create the actor */
    public static Behavior<BodyMsg> create(int totBodies) {
        nBodies = totBodies;
        return Behaviors.setup(BodyActor::new);
    }

    private void initializeBodies(int totBodies) {
        Boundary bounds =  new Boundary(-6.0, -6.0, 6.0, 6.0);
        BodyGenerator bg = new BodyGenerator();
        this.bodies = bg.generateBodies(totBodies, bounds);
    }
}
