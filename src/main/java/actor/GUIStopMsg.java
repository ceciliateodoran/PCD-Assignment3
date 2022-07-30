package actor;

import akka.actor.typed.ActorRef;

/**
 * Msg che invia la GUI per fermare gli attori
 */
public class GUIStopMsg implements ControllerMsg {
    private Boolean stop;
    private ActorRef replyTo;

    public GUIStopMsg(ActorRef replyTo) {
        this.replyTo = replyTo;
        this.stop = false;
    }

    public void setStop(Boolean stopMsg){
        this.stop = stopMsg;
    }

    public Boolean isStopped(){
        return this.stop;
    }

    public ActorRef getReplyTo() {
        return this.replyTo;
    }
}
