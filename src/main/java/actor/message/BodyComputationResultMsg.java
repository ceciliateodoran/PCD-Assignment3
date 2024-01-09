package actor.message;

import actor.utils.Body;

/**
 * Message from BodyActor to ControllerActor with the new Body
 */
public class BodyComputationResultMsg implements ControllerMsg {
    private final Body b;

    public BodyComputationResultMsg(Body b) {
        this.b = b;
    }

    public Body getBody() {
        return b;
    }
}
