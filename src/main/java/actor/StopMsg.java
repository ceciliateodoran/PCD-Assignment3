package actor;

import akka.actor.typed.ActorRef;

public class StopMsg implements BodyMsg {

    private final ActorRef<ControllerMsg> replyTo;

    public StopMsg(final ActorRef<ControllerMsg> replyTo) {
        this.replyTo = replyTo;
    }

    public ActorRef<ControllerMsg> getReplyTo() {
        return this.replyTo;
    }
}
