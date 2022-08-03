package actor;

import actor.utils.Body;
import actor.utils.Boundary;
import actor.view.ViewMsg;
import akka.actor.typed.ActorRef;

import java.util.List;

/**
 * Msg che invia il Body con le nuove posizioni di corpi
 */
public class UpdatedPositionsMsg implements ControllerMsg, ViewMsg {
    private List<Body> bodies;

    private double vt;

    private int iter;

    private Boundary bounds;

    public UpdatedPositionsMsg(final List<Body> allBodies, final Boundary bounds) {
        this.bodies = allBodies;
        this.bounds = bounds;
    }

    // Costruttore da usare per l'update della view
    public UpdatedPositionsMsg(final List<Body> allBodies, final double vt,
                               final int iter, final Boundary bounds) {
        this.bodies = allBodies;
        this.vt = vt;
        this.iter = iter;
        this.bounds = bounds;
    }

    public List<Body> getBodies(){
        return this.bodies;
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
