package distributed.model.utility;

import distributed.utils.Pair;
import java.time.ZonedDateTime;

/**
 * Represents the snapshot of a physical sensor
 */
public class SensorSnapshot {
    private final Pair<Integer, Integer> coordinates;
    private final ZonedDateTime dateTimeStamp;
    private final Double value;
    private final Double limit;
    private final String id;

    /**
     * Construct a new instance of the sensor snapshot
     *
     * @param coordinates The spatial coordinates of the sensor
     * @param value The value detected by the sensor
     * @param limit The maximum level that the water can reach
     * @param id The sensor identifier
     * @param dateTimeStamp The date and time stamp of the detected value
     */
    public SensorSnapshot(Pair<Integer, Integer> coordinates, Double value, Double limit, String id, ZonedDateTime dateTimeStamp) {
        this.coordinates = coordinates;
        this.value = value;
        this.limit = limit;
        this.id = id;
        this.dateTimeStamp = dateTimeStamp;
    }

    /**
     * @return the coordinates of the sensor
     */
    public Pair<Integer, Integer> getCoordinates() {
        return coordinates;
    }

    /**
     * @return the value detected by the sensor
     */
    public Double getValue() {
        return value;
    }

    /**
     * @return the value of the maximum level that the water can reach
     */
    public Double getLimit() {
        return limit;
    }

    /**
     * @return the sensor identifier
     */
    public String getId() {
        return id;
    }

    /**
     * @return the date and time stamp of the detected value
     */
    public ZonedDateTime getDateTimeStamp() {
        return this.dateTimeStamp;
    }

    @Override
    public String toString() {
        return "SensorSnapshot{" +
                "coordinates=" + coordinates +
                ", dateTimeStamp=" + dateTimeStamp +
                ", value=" + value +
                ", limit=" + limit +
                ", id='" + id + '\'' +
                '}';
    }
}
