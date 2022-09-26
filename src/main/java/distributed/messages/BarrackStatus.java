package distributed.messages;

import distributed.model.SensorSnapshot;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class BarrackStatus extends ValueMsg {
    private final String status;
    private final ZonedDateTime dateTimeStamp;
    private final List<SensorSnapshot> snapshot;

    public BarrackStatus(String status, ZonedDateTime dateTimeStamp, List<SensorSnapshot> snapshot) {
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

    public List<SensorSnapshot> getSnapshot() {
        return snapshot;
    }
}