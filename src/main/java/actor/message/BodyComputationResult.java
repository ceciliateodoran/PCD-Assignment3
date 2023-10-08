package actor.message;

import actor.utils.Body;

public class BodyComputationResult implements ControllerMsg {
    private final Body b;

    public BodyComputationResult(Body b) {
        this.b = b;
    }

    public Body getBody() {
        return b;
    }
}
