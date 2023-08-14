package distributed.messages.statuses;

import distributed.messages.ValueMsg;
import distributed.model.utility.SensorSnapshot;

import java.util.List;

/**
 * Message used to send the Zone status
 */
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

    /**
     * @return the zone number
     */
    public int getZone() { return zone; }

    /**
     * @return the zone status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the true or false value indicating whether the sensors snapshot is partial
     */
    public boolean getPartialData() { return partialData; }

    /**
     * @return the sensors snapshot related to the zone
     */
    public List<SensorSnapshot> getSnapshot() {
        return snapshot;
    }
}
