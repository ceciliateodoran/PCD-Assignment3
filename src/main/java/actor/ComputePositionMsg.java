package actor;

import actor.utils.Body;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;

import java.util.List;

public class ComputePositionMsg implements BodyMsg {

    private ActorRef replyToBodyActor;

    private List<Body> bodies;

    private Boundary bounds;

    public ComputePositionMsg(ActorRef bodyRef, List<Body> bodies, Boundary bounds) {
        this.replyToBodyActor = bodyRef;
        this.bodies = bodies;
        this.bounds = bounds;
    }

    public ActorRef getReplyToBodyActor() {
        return replyToBodyActor;
    }

    public List<Body> getBodies() {
        return bodies;
    }

    public Boundary getBounds() {
        return bounds;
    }
}
