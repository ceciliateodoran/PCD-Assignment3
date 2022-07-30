package actor;

import akka.actor.typed.ActorRef;

public class StopMsg implements BodyMsg, PositionCalculatorMsg, VelocityCalculatorMsg{

    private ActorRef replyTo;

    public StopMsg(ActorRef replyTo) {
        this.replyTo = replyTo;
    }

    public ActorRef getReplyTo() {
        return this.replyTo;
    }
}