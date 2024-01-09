package actor.message.test;

import actor.message.ControllerMsg;
import akka.actor.typed.ActorRef;
import distributed.messages.ValueMsg;

public class StartTest extends ValueMsg implements ControllerMsg {
    private ActorRef<ValueMsg> ref;
    private boolean noGuiTest;

    public StartTest(final ActorRef<ValueMsg> probeRef, final boolean noGui) {
        this.ref = probeRef;
        this.noGuiTest = noGui;
    }

    public ActorRef<ValueMsg> getRef() {
        return this.ref;
    }

    public boolean getNoGuiTest() {
        return this.noGuiTest;
    }
}
