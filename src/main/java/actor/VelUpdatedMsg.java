package actor;

import actor.utils.Body;

import java.util.List;

public class VelUpdatedMsg implements BodyMsg {

    List<Body> splittedBodiesUpdated;

    public VelUpdatedMsg(final List<Body> updatedVel) {
        this.splittedBodiesUpdated = updatedVel;
    }

    public List<Body> getSplittedBodiesUpdated() {
        return splittedBodiesUpdated;
    }
}
