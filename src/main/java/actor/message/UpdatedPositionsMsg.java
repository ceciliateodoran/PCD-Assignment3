package actor.message;

import actor.utils.Body;
import actor.utils.Boundary;

import java.util.ArrayList;
import java.util.List;

/**
 * Message from ControllerActor to ViewActor
 * with the new values computed for each Body and the simulation environment
 */
public class UpdatedPositionsMsg implements ControllerMsg, ViewMsg {
    private final List<Body> bodies;
    private final double vt;
    private final int iter;
    private final Boundary bounds;

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
