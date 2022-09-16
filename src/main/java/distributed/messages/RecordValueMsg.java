package distributed.messages;

import akka.actor.typed.ActorRef;
import akka.actor.typed.receptionist.Receptionist;

public class RecordValueMsg extends ValueMsg {

    private ActorRef<ValueMsg> replyToCoordinator;

    public RecordValueMsg(final ActorRef<ValueMsg> c) {
        this.replyToCoordinator = c;
    }

    public ActorRef<ValueMsg> getReplyToCoordinator() {
        return this.replyToCoordinator;
    }

}
