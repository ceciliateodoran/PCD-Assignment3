package actor;

import actor.utils.Body;

public class UpdateBody implements BodyMsg {
    private Body body;

    public UpdateBody(final Body b) {
        this.body = b;
    }

    public Body getBody() {
        return body;
    }
}
