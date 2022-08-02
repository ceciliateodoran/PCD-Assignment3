package actor;

import actor.utils.Body;

import java.util.List;

/**
 * Msg che invia il PositionActor con la nuova posizione calcolata
 */
public class UpdatePositionMsg implements BodyMsg {

    private final List<Body> updatedBodies;

    private final double dt;

    public UpdatePositionMsg(final List<Body> body, final double dt) {
        this.updatedBodies = body;
        this.dt = dt;
    }

    public List<Body> getUpdatedBodies() {
        return this.updatedBodies;
    }

    public double getDt() {
        return this.dt;
    }
}
