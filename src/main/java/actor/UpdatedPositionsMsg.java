package actor;

import actor.utils.Body;
import actor.utils.Boundary;

import java.util.List;
import java.util.Map;

/**
 * messaggio con i nuovi valori dei Bodies calcolati
 * inviato dal BodyActor al ControllerActor
 * e dal ControllerActor al ViewActor
 */
public class UpdatedPositionsMsg implements ControllerMsg, ViewMsg {
    private Map<Integer, Body> indexBodyMap;
    private List<Body> bodies;
    private double vt;

    private int iter;

    private Boundary bounds;

    /* costruttore per l'invio dei nuovi valori dei Bodies calcolati al ControllerActor */
    public UpdatedPositionsMsg(final Map<Integer, Body> bodyMap) {
        this.indexBodyMap = bodyMap;
    }

    /* costruttore per l'invio dei nuovi valori dei Bodies calcolati al ViewActor */
    public UpdatedPositionsMsg(final List<Body> allBodies, final double vt,
                               final int iter, final Boundary bounds) {
        this.bodies = allBodies;
        this.vt = vt;
        this.iter = iter;
        this.bounds = bounds;
    }

    public Map<Integer, Body> getIndexBodyMap() {
        return this.indexBodyMap;
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
