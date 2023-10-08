package actor.message;

import actor.utils.Body;
import actor.utils.Boundary;

import java.util.ArrayList;
import java.util.List;

/**
 * messaggio con i nuovi valori dei Bodies calcolati
 * inviato dal BodyActor al ControllerActor
 * e dal ControllerActor al ViewActor
 */
public class UpdatedPositionsMsg implements ControllerMsg, ViewMsg {
    private final List<Body> bodies;
    private final double vt;
    private final int iter;
    private final Boundary bounds;

    /* costruttore per l'invio dei nuovi valori dei Bodies calcolati al ViewActor */
    public UpdatedPositionsMsg(final List<Body> allBodies, final double vt,
                               final int iter, final Boundary bounds) {
        this.bodies = new ArrayList<>(allBodies);
        this.vt = vt;
        this.iter = iter;
        this.bounds = bounds;
    }

    public List<Body> getBodies() {
        return bodies;
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
