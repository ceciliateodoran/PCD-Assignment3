package distributed.messages;

import distributed.model.SensorSnapshot;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public class ZoneStatus extends ValueMsg {
    private final String status;
    private final List<SensorSnapshot> snapshot;

    public ZoneStatus(String status, List<SensorSnapshot> snapshot) {
        this.status = status;
        this.snapshot = snapshot;
    }

    public String getStatus() {
        return status;
    }

    public List<SensorSnapshot> getSnapshot() {
        return snapshot;
    }
}
