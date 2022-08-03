package actor;

import akka.actor.typed.ActorRef;

/**
 * messaggio che invia il ControllerActor al BodyActor
 * per far iniziare il nuovo calcolo di velocità e posizione
 */
public class ComputePositionsMsg implements BodyMsg {

    private ActorRef replyTo;

    /* virtual time step */
    private final double dt;

    public ComputePositionsMsg(final ActorRef replyTo, final double dt) {
        this.replyTo = replyTo;
        this.dt = dt;
    }

    public ActorRef getReplyTo() {
        return this.replyTo;
    }

    public double getDt() {
        return this.dt;
    }
}
