package actor;

import actor.utils.Body;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;

import java.util.Collections;
import java.util.List;

public class ComputeVelocityMsg implements VelocityCalculationMsg {
    private final ActorRef<BodyMsg> replyTo;

    private final ActorRef<PositionCalculationMsg> posActorRef;

    private final Body currentBody;

    private final List<Body> bodies;

    private final Boundary bounds;

    /* virtual time step */
    private final double dt;

    public ComputeVelocityMsg(final ActorRef<BodyMsg> replyTo, final ActorRef<PositionCalculationMsg> posActor,
                              final Body b, final List<Body> allBodies, final Boundary bounds, final double dt) {
        this.replyTo = replyTo;
        this.posActorRef = posActor;
        this.currentBody = b;
        this.bodies = allBodies;
        this.bounds = bounds;
        this.dt = dt;
    }

    public ActorRef<BodyMsg> getReplyTo() {
        return this.replyTo;
    }

    public Body getBody() {
        return this.currentBody;
    }

    public List<Body> getBodies() {
        return Collections.unmodifiableList(this.bodies);
    }

    public ActorRef<PositionCalculationMsg> getPosActorRef() {
        return this.posActorRef;
    }

    public Boundary getBounds() {
        return this.bounds;
    }

    public double getDt() {
        return this.dt;
    }
}
