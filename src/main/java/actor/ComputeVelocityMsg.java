package actor;

import actor.utils.Body;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;

import java.util.Collections;
import java.util.List;

public class ComputeVelocityMsg implements VelocityCalculationMsg {
    private ActorRef replyTo;

    private ActorRef posActorRef;
    private Body currentBody;
    private List<Body> bodies;
    private Boundary bounds;

    private int index;

    /* virtual time step */
    private final double dt;

    public ComputeVelocityMsg(ActorRef replyTo, ActorRef posActor, Body b, List<Body> allBodies, Boundary bounds, double dt) {
        this.replyTo = replyTo;
        this.posActorRef = posActor;
        this.currentBody = b;
        this.bodies = allBodies;
        this.bounds = bounds;
        this.dt = dt;
    }

    public ActorRef getReplyTo() {
        return this.replyTo;
    }

    public Body getBody() {
        return this.currentBody;
    }

    public List<Body> getBodies() {
        return Collections.unmodifiableList(this.bodies);
    }

    public ActorRef getPosActorRef() {
        return this.posActorRef;
    }

    public Boundary getBounds() {
        return this.bounds;
    }

    public double getDt() {
        return this.dt;
    }
}
