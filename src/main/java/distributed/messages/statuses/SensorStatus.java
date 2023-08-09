package distributed.messages.statuses;

import distributed.messages.ValueMsg;
import distributed.utils.Pair;

import java.time.ZonedDateTime;

/**
 * Message used to send the Sensor status
 */
public class SensorStatus extends ValueMsg {

    private double waterLevel;
    private double limit;
    private ZonedDateTime dateTimeStamp;
    private int sensorZone;
    private String sensorID;
    private Pair<Integer, Integer> sensorCoords;
    private String seqNumber;

    public SensorStatus(final int zone, final String sensorID, final double value, final double limit, final Pair<Integer, Integer> sensorCoords, String seqNumber) {
        this.sensorZone = zone;
        this.sensorID = sensorID;
        this.waterLevel = value;
        this.dateTimeStamp = ZonedDateTime.now();
        this.limit = limit;
        this.sensorCoords = sensorCoords;
        this.seqNumber = seqNumber;
    }

    /**
     * @return the sensor coordinates
     */
    public Pair<Integer, Integer> getSensorCoords() {
        return this.sensorCoords;
    }

    /**
     * @return the water level
     */
    public double getWaterLevel() {
        return waterLevel;
    }

    /**
     * @return the water limit value of the sensor
     */
    public double getLimit() {
        return limit;
    }

    /**
     * @return the date and time stamp
     */
    public ZonedDateTime getDateTimeStamp() {
        return dateTimeStamp;
    }

    /**
     * @return the sensor identifier
     */
    public String getSensorID() {
        return sensorID;
    }

    /**
     * @return the sequence number related to the sensor
     */
    public String getSeqNumber(){
        return seqNumber;
    }

    /**
     * @return the sensor status in a string
     */
    @Override
    public String toString() {
        return "DetectedValueMsg{" +
                "waterLevel=" + waterLevel +
                ", limit=" + limit +
                ", dateTimeStamp=" + dateTimeStamp +
                ", sensorZone=" + sensorZone +
                ", sensorID='" + sensorID + '\'' +
                ", sensorCoords=" + sensorCoords +
                '}';
    }
}
