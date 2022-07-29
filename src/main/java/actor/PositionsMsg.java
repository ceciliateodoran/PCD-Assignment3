package actor;

import actor.utils.P2d;
import akka.actor.typed.ActorRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Msg che invia il Body con le nuove posizioni di corpi
 */
public class PositionsMsg implements ControllerMsg {
    private List<P2d> positions;
    private ActorRef replyTo;

    public PositionsMsg(ActorRef replyTo) {
        this.replyTo = replyTo;
        this.positions = new ArrayList<>();
    }

    public List<P2d> getPositions(){
        return Collections.unmodifiableList(this.positions);
    }

    public void setPositions(List<P2d> newPositions){
        this.positions = newPositions;
    }

    public ActorRef getReplyTo() {
        return this.replyTo;
    }
}
