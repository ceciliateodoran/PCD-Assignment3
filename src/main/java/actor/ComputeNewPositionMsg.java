package actor;

import actor.utils.Body;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;

import java.util.List;

public class ComputeNewPositionMsg implements PositionCalculationMsg {

    private final ActorRef<BodyMsg> replyTo;

    private final List<Body> bodies;

    private final Body currentBody;

    private static double dt;

    private final Boundary bounds;

    public ComputeNewPositionMsg(ActorRef<BodyMsg> replyTo, Body b, List<Body> allBodies, double deltaT, Boundary bounds) {
        this.replyTo = replyTo;
        this.currentBody = b;
        this.bodies = allBodies;
        dt = deltaT;
        this.bounds = bounds;
    }

    public ActorRef<BodyMsg> getReplyTo() {
        return replyTo;
    }

    public List<Body> getBodies() {
        return bodies;
    }

    public Body getCurrentBody() {
        return this.currentBody;
    }

    public double getDt() {
        return dt;
    }

    public Boundary getBounds() {
        return this.bounds;
    }
}
