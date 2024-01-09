package actor.message.test;

import actor.message.ControllerMsg;
import actor.utils.Body;
import distributed.messages.ValueMsg;

import java.util.Collections;
import java.util.List;

public class SerialTestResult extends ValueMsg implements ControllerMsg {
    private List<Body> bodies;

    public SerialTestResult(final List<Body> bodiesList) {
        this.bodies = bodiesList;
    }

    public List<Body> getBodies() {
        return this.bodies;
    }
}
