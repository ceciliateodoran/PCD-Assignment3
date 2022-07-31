package actor;

import actor.utils.Body;
import actor.utils.P2d;
import akka.actor.typed.ActorRef;

/**
 * Msg che invia il PositionActor con la nuova posizione calcolata
 */
public class UpdatePositionMsg implements BodyMsg {

    private Body updatedBody;

    private int bodyIndex;

    private double dt;

    public UpdatePositionMsg(Body body, int index, double dt) {
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
