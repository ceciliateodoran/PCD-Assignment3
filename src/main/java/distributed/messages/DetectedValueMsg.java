package distributed.messages;

import akka.actor.typed.pubsub.Topic.Command;
import distributed.model.Sensor;

import java.time.ZonedDateTime;

public class DetectedValueMsg extends ValueMsg {

    private double waterLevel;
    private ZonedDateTime dateTimeStamp;
    private int sensorZone;
    private int sensorId;

    public DetectedValueMsg(final int zone, final int id, final double value) {
        this.sensorZone = zone;
        this.sensorId = id;
        this.waterLevel = value;
        this.dateTimeStamp = ZonedDateTime.now();
    }

    @Override
    public String toString() {
        return "DetectedValueMsg{" +
                "sensorId=" + sensorId +
                ", sensorZone=" + sensorZone +
                ", waterLevel=" + waterLevel +
                ", dateTimeStamp=" + dateTimeStamp +
                '}';
    }
}
