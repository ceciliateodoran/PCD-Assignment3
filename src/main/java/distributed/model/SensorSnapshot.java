package distributed.model;

import akka.japi.Pair;

public class SensorSnapshot {
    private final Pair<Integer, Integer> coordinates;
    private final Double value;
    private final Double limit;
    private final String id;

    public SensorSnapshot(Pair<Integer, Integer> coordinates, Double value, Double limit, String id) {
        this.coordinates = coordinates;
        this.value = value;
        this.limit = limit;
        this.id = id;
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
}
