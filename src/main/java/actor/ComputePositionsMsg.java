package actor;

import actor.utils.Boundary;
import akka.actor.typed.ActorRef;

import java.util.List;

/**
 * messaggio che invia il ControllerActor al BodyActor
 * per far iniziare il nuovo calcolo di velocità e posizione
 */
public class ComputePositionsMsg implements BodyMsg {

    private ActorRef<ControllerMsg> replyTo;
    private List<ActorRef<BodyMsg>> bodyRefsList;
    private Boundary bounds;

    /* virtual time step */
    private final double dt;

    public ComputePositionsMsg(final ActorRef<ControllerMsg> replyTo, final double dt, final List<ActorRef<BodyMsg>> refsList, final Boundary bounds) {
        this.replyTo = replyTo;
        this.dt = dt;
        this.bodyRefsList = refsList;
        this.bounds = bounds;
    }

    public ActorRef<ControllerMsg> getReplyTo() {
        return this.replyTo;
    }

    public double getDt() {
        return this.dt;
    }

    public List<ActorRef<BodyMsg>> getBodyRefsList() {
        return bodyRefsList;
    }

    public Boundary getBounds() {
        return bounds;
    }
}
