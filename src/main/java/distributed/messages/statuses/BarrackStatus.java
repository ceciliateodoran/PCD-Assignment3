package distributed.messages.statuses;


import com.fasterxml.jackson.annotation.JsonProperty;
import distributed.messages.ValueMsg;
import distributed.model.utility.SensorSnapshot;
import distributed.utils.Pair;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Message used to send the Barrack status
 */
public class BarrackStatus extends ValueMsg {
    private final String status;
    private final ZonedDateTime dateTimeStamp;
    private final int zone;
    @JsonProperty("sensorValues")
    private final Pair<List<SensorSnapshot>, Boolean> sensorValues;

    public BarrackStatus(String status, ZonedDateTime dateTimeStamp, int zone, @JsonProperty("sensorValues") Pair<List<SensorSnapshot>, Boolean> sensorValues) {
        this.status = status;
        this.dateTimeStamp = dateTimeStamp;
        this.zone = zone;
        this.sensorValues = sensorValues;
    }

    /**
     * @return the status of the Barrack
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the zone number
     */
    public int getZone() { return zone; }

    /**
     * @return the date and time stamp
     */
    public ZonedDateTime getDateTimeStamp() {
        return dateTimeStamp;
    }

    /**
     * @return the sensor values related to the barrack
     */
    @JsonProperty("sensorValues")
    public Pair<List<SensorSnapshot>, Boolean> getSensorValues() { return sensorValues; }
}