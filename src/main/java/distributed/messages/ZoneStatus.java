package distributed.messages;

import distributed.model.SensorSnapshot;

import java.util.List;

public class ZoneStatus extends ValueMsg {
    private final int zone;
    private final String status;
    private final List<SensorSnapshot> snapshot;
    private final boolean partialData;

    public ZoneStatus(int zone, String status, List<SensorSnapshot> snapshot, boolean partialData) {
        this.zone = zone;
        this.status = status;
        this.snapshot = snapshot;
        this.partialData = partialData;
    }

    public int getZone() { return zone; }
    public String getStatus() {
        return status;
    }
    public boolean getPartialData() { return partialData; }
    public List<SensorSnapshot> getSnapshot() {
        return snapshot;
    }
}
