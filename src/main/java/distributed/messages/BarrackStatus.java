package distributed.messages;

import java.time.ZonedDateTime;
import java.util.Map;

public class BarrackStatus extends ValueMsg {
    private final String status;
    private final ZonedDateTime dateTimeStamp;
    private final Map<String, Double> snapshot;

    public BarrackStatus(String status, ZonedDateTime dateTimeStamp, Map<String, Double> snapshot) {
        this.status = status;
        this.dateTimeStamp = dateTimeStamp;
        this.snapshot = snapshot;
    }

    public String getStatus() {
        return status;
    }

    public ZonedDateTime getDateTimeStamp() {
        return dateTimeStamp;
    }

    public Map<String, Double> getSnapshot() {
        return snapshot;
    }
}