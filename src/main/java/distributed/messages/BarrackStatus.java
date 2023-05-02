package distributed.messages;

import java.time.ZonedDateTime;

public class BarrackStatus extends ValueMsg {
    private final String status;
    private final ZonedDateTime dateTimeStamp;
    private final int zone;

    public BarrackStatus(String status, ZonedDateTime dateTimeStamp, int zone) {
        this.status = status;
        this.dateTimeStamp = dateTimeStamp;
        this.zone = zone;
    }

    public String getStatus() {
        return status;
    }
    public int getZone() { return zone; }
    public ZonedDateTime getDateTimeStamp() {
        return dateTimeStamp;
    }
}