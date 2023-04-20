package distributed.messages;

public class RequestSensorDataMsg extends ValueMsg{
    private final String seqNumber;

    public RequestSensorDataMsg(String seqNumber) {
        this.seqNumber = seqNumber;
    }
    public String getSeqNumber() {
        return seqNumber;
    }
}
