package actor;

import akka.actor.typed.ActorRef;

public class StopMsg implements BodyMsg, PositionCalculationMsg, VelocityCalculationMsg {

    private final ActorRef<ControllerMsg> replyTo;

    public StopMsg(ActorRef<ControllerMsg> replyTo) {
        this.replyTo = replyTo;
    }

    public ActorRef<ControllerMsg> getReplyTo() {
        return this.replyTo;
    }
}
