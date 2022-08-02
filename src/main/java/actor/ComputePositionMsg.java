package actor;

import akka.actor.typed.ActorRef;

/**
 * Msg che invia il Controller per la nuova iterazione
 */
public class ComputePositionMsg implements BodyMsg {

    private ActorRef replyTo;

    /* virtual time step */
    private final double dt;

    public ComputePositionMsg(final ActorRef replyTo, final double dt) {
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
