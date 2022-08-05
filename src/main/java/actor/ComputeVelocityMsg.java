package actor;

import actor.utils.Body;
import akka.actor.typed.ActorRef;

import java.util.List;

public class ComputeVelocityMsg implements BodyMsg {

    private ActorRef replyToBodyActor;

    private List<Body> bodies;

    public ComputeVelocityMsg(ActorRef bodyRef, List<Body> bodies) {
        this.replyToBodyActor = bodyRef;
        this.bodies = bodies;
    }

    public ActorRef getReplyToBodyActor() {
        return replyToBodyActor;
    }

    public List<Body> getBodies() {
        return bodies;
    }
}
