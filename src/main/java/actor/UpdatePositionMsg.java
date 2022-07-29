package actor;

import akka.actor.typed.ActorRef;

/**
 * Msg che invia il PositionActor con la nuova posizione calcolata
 */
public class UpdatePositionMsg implements BodyMsg {

    private ActorRef replyTo;

    public UpdatePositionMsg(ActorRef replyTo) {
        this.replyTo = replyTo;
    }

    public ActorRef getReplyTo() {
        return this.replyTo;
    }
}
