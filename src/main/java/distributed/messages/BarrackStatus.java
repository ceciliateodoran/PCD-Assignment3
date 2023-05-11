package distributed.messages;

import akka.japi.Pair;
import distributed.model.SensorSnapshot;

import java.time.ZonedDateTime;
import java.util.List;

public class BarrackStatus extends ValueMsg {
    private final String status;
    private final ZonedDateTime dateTimeStamp;
    private final int zone;
    private final Pair<List<SensorSnapshot>, Boolean> sensorValues;

    public BarrackStatus(String status, ZonedDateTime dateTimeStamp, int zone, Pair<List<SensorSnapshot>, Boolean> sensorValues) {
        this.status = status;
        this.dateTimeStamp = dateTimeStamp;
        this.zone = zone;
        this.sensorValues = sensorValues;
    }

    public String getStatus() {
        return status;
    }
    public int getZone() { return zone; }
    public ZonedDateTime getDateTimeStamp() {
        return dateTimeStamp;
    }
    public Pair<List<SensorSnapshot>, Boolean> getSensorValues() { return sensorValues; }
}