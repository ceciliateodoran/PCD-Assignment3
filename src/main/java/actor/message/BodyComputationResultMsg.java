package actor.message;

import actor.utils.Body;

import java.util.List;

/**
 * Message from BodyActor to ControllerActor with the new Body
 */
public class BodyComputationResultMsg implements ControllerMsg {
    private final List<Body> b;
    private final Integer runNumber;

    public BodyComputationResultMsg(List<Body> b, Integer runNumber) {
        this.b = b;
        this.runNumber = runNumber;
    }

    public List<Body> getBody() {
        return b;
    }
    public Integer getRunNumber() { return runNumber; }
}
