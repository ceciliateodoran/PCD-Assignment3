package actor;

import actor.utils.Body;

/**
 * Msg che invia il PositionActor con la nuova posizione calcolata
 */
public class UpdatePositionMsg implements BodyMsg {

    private final Body updatedBody;

    private final int bodyIndex;

    private final double dt;

    public UpdatePositionMsg(final Body body, final int index, final double dt) {
        this.updatedBody = body;
        this.bodyIndex = index;
        this.dt = dt;
    }

    public Body getUpdatedBody() {
        return this.updatedBody;
    }

    public int getBodyIndex() {
        return this.bodyIndex;
    }

    public double getDt() {
        return this.dt;
    }
}
