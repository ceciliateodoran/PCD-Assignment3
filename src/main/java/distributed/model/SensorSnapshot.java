package distributed.model;

import akka.japi.Pair;

import java.time.ZonedDateTime;

public class SensorSnapshot {
    private final Pair<Integer, Integer> coordinates;
    private final ZonedDateTime dateTimeStamp;
    private final Double value;
    private final Double limit;
    private final String id;

    public SensorSnapshot(Pair<Integer, Integer> coordinates, Double value, Double limit, String id, ZonedDateTime dateTimeStamp) {
        this.coordinates = coordinates;
        this.value = value;
        this.limit = limit;
        this.id = id;
        this.dateTimeStamp = dateTimeStamp;
    }

    public Pair<Integer, Integer> getCoordinates() {
        return coordinates;
    }

    public Double getValue() {
        return value;
    }

    public Double getLimit() {
        return limit;
    }

    public String getId() {
        return id;
    }

    public ZonedDateTime getDateTimeStamp() {
        return this.dateTimeStamp;
    }
}
