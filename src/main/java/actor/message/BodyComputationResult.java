package actor.message;

import actor.utils.Body;

/**
 * Message from BodyActor to ControllerActor with the new Body
 */
public class BodyComputationResult implements ControllerMsg {
    private final Body b;

    public BodyComputationResult(Body b) {
        this.b = b;
    }

    public Body getBody() {
        return b;
    }
}
