package actor;

import actor.utils.Body;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;

import java.util.List;

/**
 * Msg che invia il Body con le nuove posizioni di corpi
 */
public class PositionsMsg implements ControllerMsg, ViewMsg {
    private List<Body> bodies;

    private ActorRef<BodyMsg> replyTo;

    private double dt;

    private double vt;

    private int iter;

    private Boundary bounds;

    public PositionsMsg(final ActorRef<BodyMsg> replyTo, final List<Body> allBodies,
                        final double dt, final Boundary bounds) {
        this.replyTo = replyTo;
        this.bodies = allBodies;
        this.dt = dt;
        this.bounds = bounds;
    }

    // Costruttore da usare per l'update della view
    public PositionsMsg(final List<Body> allBodies, final double vt,
                        final int iter, final Boundary bounds) {
        this.bodies = allBodies;
        this.vt = vt;
        this.iter = iter;
        this.bounds = bounds;
    }

    public List<Body> getBodies(){
        return this.bodies;
    }

    public ActorRef getReplyTo() {
        return this.replyTo;
    }

    public double getDt() {
        return this.dt;
    }

    public double getVt() {
        return this.vt;
    }

    public int getIter() {
        return this.iter;
    }

    public Boundary getBounds() {
        return this.bounds;
    }
}
