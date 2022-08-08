package actor;

import actor.utils.Body;
import akka.actor.typed.ActorRef;

import java.util.List;

public class ComputeVelocityMsg implements BodyMsg {

    private ActorRef<BodyMsg> replyToBodyActor;

    private List<Body> bodies;

    public ComputeVelocityMsg(ActorRef<BodyMsg> bodyRef, List<Body> bodies) {
        this.replyToBodyActor = bodyRef;
        this.bodies = bodies;
    }

    public ActorRef<BodyMsg> getReplyToBodyActor() {
        return replyToBodyActor;
    }

    public List<Body> getBodies() {
        return bodies;
    }
}
