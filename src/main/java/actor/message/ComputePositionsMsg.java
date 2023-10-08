package actor.message;

import actor.message.BodyMsg;
import actor.message.ControllerMsg;
import actor.utils.Body;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;

import java.util.ArrayList;
import java.util.List;

/**
 * messaggio che invia il ControllerActor al BodyActor
 * per far iniziare il nuovo calcolo di velocità e posizione
 */
public class ComputePositionsMsg implements BodyMsg {

    private ActorRef<ControllerMsg> replyTo;
    private List<Body> bodyList;
    private Boundary bounds;

    /* virtual time step */
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
