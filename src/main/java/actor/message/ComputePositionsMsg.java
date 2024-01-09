package actor.message;

import actor.utils.Body;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;

import java.util.ArrayList;
import java.util.List;

/**
 * Message from ControllerActor to BodyActor
 * to start the computation of new velocity and position values
 */
public class ComputePositionsMsg implements BodyMsg {

    private final ActorRef<ControllerMsg> replyTo;
    private final List<Body> bodyList;
    private final Boundary bounds;
    private final double dt;

    public ComputePositionsMsg(final ActorRef<ControllerMsg> replyTo, final double dt, final List<Body> bodyList, final Boundary bounds) {
        this.replyTo = replyTo;
        this.dt = dt;
        this.bodyList = new ArrayList<>(bodyList);
        this.bounds = bounds;
    }

    public ActorRef<ControllerMsg> getReplyTo() {
        return this.replyTo;
    }

    public double getDt() {
        return this.dt;
    }

    public List<Body> getBodyList() {
        return bodyList;
    }

    public Boundary getBounds() {
        return bounds;
    }
}
