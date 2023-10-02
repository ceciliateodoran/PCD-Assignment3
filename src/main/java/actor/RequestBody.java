package actor;

import akka.actor.typed.ActorRef;

public class RequestBody implements BodyMsg {
    private ActorRef<BodyMsg> replyTo;
    public RequestBody(final ActorRef<BodyMsg> ref) {
        this.replyTo = ref;
    }

    public ActorRef<BodyMsg> getReplyTo() {
        return replyTo;
    }
}
