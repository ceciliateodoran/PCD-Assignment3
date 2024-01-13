package actor.message;

import actor.utils.Body;

/**
 * Message from BodyActor to ControllerActor with the new Body
 */
public class BodyComputationResultMsg implements ControllerMsg {
    private final Body b;
    private final Integer runNumber;

    public BodyComputationResultMsg(Body b, Integer runNumber) {
        this.b = b;
        this.runNumber = runNumber;
    }

    public Body getBody() {
        return b;
    }
    public Integer getRunNumber() { return runNumber; }
}
