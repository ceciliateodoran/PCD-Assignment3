package actor;

import actor.utils.Body;
import actor.utils.Boundary;

import java.util.List;

/**
 * messaggio con i nuovi valori dei Bodies calcolati
 * inviato dal BodyActor al ControllerActor
 * e dal ControllerActor al ViewActor
 */
public class UpdatedPositionsMsg implements ControllerMsg, ViewMsg {
    private List<Body> bodies;

    private double vt;

    private int iter;

    private Boundary bounds;

    /* costruttore per l'invio dei nuovi valori dei Bodies calcolati al ControllerActor */
    public UpdatedPositionsMsg(final List<Body> allBodies, final Boundary bounds) {
        this.bodies = allBodies;
        this.bounds = bounds;
    }

    /* costruttore per l'invio dei nuovi valori dei Bodies calcolati al ViewActor */
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
