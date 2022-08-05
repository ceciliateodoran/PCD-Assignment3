package actor;

import actor.utils.Body;

import java.util.List;

public class PosUpdatedMsg implements BodyMsg {
    List<Body> splittedBodiesUpdated;

    public PosUpdatedMsg(final List<Body> updatedPos) {
        this.splittedBodiesUpdated = updatedPos;
    }

    public List<Body> getSplittedBodiesUpdated() {
        return splittedBodiesUpdated;
    }
}
