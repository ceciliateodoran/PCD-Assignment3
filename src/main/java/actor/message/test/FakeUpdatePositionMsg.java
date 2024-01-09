package actor.message.test;

import actor.message.ControllerMsg;
import akka.actor.typed.ActorRef;
import distributed.messages.ValueMsg;

public class FakeUpdatePositionMsg extends ValueMsg implements ControllerMsg {
    private ActorRef<ControllerMsg> controllerRef;

    public FakeUpdatePositionMsg(final ActorRef<ControllerMsg> ref) {
        this.controllerRef = ref;
    }

    public ActorRef<ControllerMsg> getControllerRef()  {
        return this.controllerRef;
    }
}
