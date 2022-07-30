package actor;

import actor.utils.Body;
import akka.actor.typed.ActorRef;

public class ComputeVelocityMsg implements BodyMsg {
    private ActorRef replyTo;
    private Body body;
    private int index;

    public ComputeVelocityMsg(ActorRef replyTo, Body b, int bodyIndex) {
        this.replyTo = replyTo;
        this.body = b;
        this.index = bodyIndex;
    }

    public ActorRef getReplyTo() {
        return this.replyTo;
    }

    public Body getBody() {
        return this.body;
    }

    public int getIndex() {
        return this.index;
    }
}
