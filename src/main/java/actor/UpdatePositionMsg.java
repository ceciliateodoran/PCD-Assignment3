package actor;

import actor.utils.Body;
import actor.utils.P2d;
import akka.actor.typed.ActorRef;

/**
 * Msg che invia il PositionActor con la nuova posizione calcolata
 */
public class UpdatePositionMsg implements BodyMsg {

    private ActorRef replyTo;

    private Body updatedBody;

    private int bodyIndex;

    public UpdatePositionMsg(ActorRef replyTo, Body body, int index) {
        this.replyTo = replyTo;
        this.updatedBody = body;
        this.bodyIndex = index;
    }

    public ActorRef getReplyTo() {
        return this.replyTo;
    }

    public Body getUpdatedBody() {
        return this.updatedBody;
    }

    public int getBodyIndex() {
        return this.bodyIndex;
    }
}
