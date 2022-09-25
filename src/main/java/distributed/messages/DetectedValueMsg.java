package distributed.messages;

import akka.japi.Pair;

import java.time.ZonedDateTime;

public class DetectedValueMsg extends ValueMsg {

    private double waterLevel;
    private ZonedDateTime dateTimeStamp;
    private int sensorZone;
    private String sensorID;
    private Pair<Integer, Integer> sensorCoords;

    public DetectedValueMsg(final int zone, final String sensorID, final double value, final Pair<Integer, Integer> sensorCoords) {
        this.sensorZone = zone;
        this.sensorID = sensorID;
        this.waterLevel = value;
        this.dateTimeStamp = ZonedDateTime.now();
        this.sensorCoords = sensorCoords;
    }

    public Pair<Integer, Integer> getSensorCoords() {
        return this.sensorCoords;
    }

    @Override
    public String toString() {
        return "DetectedValueMsg{" +
                "waterLevel=" + waterLevel +
                ", dateTimeStamp=" + dateTimeStamp +
                ", sensorZone=" + sensorZone +
                ", sensorID='" + sensorID + '\'' +
                ", sensorCoords=" + sensorCoords +
                '}';
    }
}
