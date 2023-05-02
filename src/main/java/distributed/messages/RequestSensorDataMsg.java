package distributed.messages;

import akka.actor.typed.ActorRef;

public class RequestSensorDataMsg extends ValueMsg{
    private final String seqNumber;
    private final ActorRef<ValueMsg> replyTo;

    public RequestSensorDataMsg(String seqNumber, ActorRef<ValueMsg> replyTo) {
        this.seqNumber = seqNumber;
        this.replyTo = replyTo;
    }

    public String getSeqNumber() {
        return seqNumber;
    }
    public ActorRef<ValueMsg> getReplyTo() { return replyTo; }
}
