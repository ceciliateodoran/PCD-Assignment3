package distributed.messages.spawn;

import distributed.messages.ValueMsg;
import distributed.utils.Pair;

/**
 * Message used to spawn a new Sensor actor
 */
public class SpawnSensorActor extends ValueMsg {
    private final String id;
    private final int zoneNumber;
    private final Pair<Integer, Integer> sensorCoords;
    private final double limit;
    private final int sensorCounter;

    public SpawnSensorActor(String id, int zoneNumber, Pair<Integer, Integer> sensorCoords, double limit, int sensorCounter) {
        this.id = id;
        this.zoneNumber = zoneNumber;
        this.sensorCoords = sensorCoords;
        this.limit = limit;
        this.sensorCounter = sensorCounter;
    }

    /**
     * @return the sensor actor identifier
     */
    public String getId() {
        return id;
    }

    /**
     * @return the zone number
     */
    public int getZoneNumber() {
        return zoneNumber;
    }

    /**
     * @return the sensor coordinates
     */
    public Pair<Integer, Integer> getSensorCoords() {
        return sensorCoords;
    }

    /**
     * @return the water limit value for the sensor zone
     */
    public double getLimit() {
        return limit;
    }

    /**
     * @return the counter of the sensor
     */
    public int getSensorCounter() {
        return sensorCounter;
    }
}
