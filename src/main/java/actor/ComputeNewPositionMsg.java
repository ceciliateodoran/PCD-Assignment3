package actor;

import actor.utils.Body;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;

import java.util.List;

public class ComputeNewPositionMsg implements PositionCalculationMsg {

    private static double dt;

    private final ActorRef<BodyMsg> replyTo;

    private final List<Body> bodies;

    private final Boundary bounds;

    public ComputeNewPositionMsg(final ActorRef<BodyMsg> replyTo, final List<Body> allBodies,
                                 final double deltaT, final Boundary bounds) {
        this.replyTo = replyTo;
        this.bodies = allBodies;
        this.dt = deltaT;
        this.bounds = bounds;
    }

    public ActorRef<BodyMsg> getReplyTo() {
        return this.replyTo;
    }

    public List<Body> getBodies() {
        return this.bodies;
    }

    public double getDt() {
        return this.dt;
    }

    public Boundary getBounds() {
        return this.bounds;
    }
}
