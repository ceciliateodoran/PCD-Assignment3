package actor;

import actor.utils.Body;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;

import java.util.List;

public class ComputeNewPositionMsg implements PositionCalculationMsg {

    private ActorRef replyTo;

    private List<Body> bodies;

    private Body currentBody;

    private static double dt;

    private Boundary bounds;

    public ComputeNewPositionMsg(ActorRef replyTo, Body b, List<Body> allBodies, double dt, Boundary bounds) {
        this.replyTo = replyTo;
        this.currentBody = b;
        this.bodies = allBodies;
        this.dt = dt;
        this.bounds = bounds;
    }

    public ActorRef getReplyTo() {
        return replyTo;
    }

    public List<Body> getBodies() {
        return bodies;
    }

    public Body getCurrentBody() {
        return this.currentBody;
    }

    public double getDt() {
        return this.dt;
    }

    public Boundary getBounds() {
        return this.bounds;
    }
}
