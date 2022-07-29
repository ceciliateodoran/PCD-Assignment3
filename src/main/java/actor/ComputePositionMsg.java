package actor;

import akka.actor.typed.ActorRef;

public class ComputePositionMsg implements BodyMsg {
    private ActorRef replyTo;

    public ComputePositionMsg(ActorRef replyTo) {
        this.replyTo = replyTo;
    }

    public ActorRef getReplyTo() {
        return this.replyTo;
    }
}
