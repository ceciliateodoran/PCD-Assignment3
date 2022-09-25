package distributed;

import akka.japi.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CityZone {

    private String idZone;
    private int index;
    private int x;
    private int y;
    private int xOffset;
    private int yOffset;
    private Map<String, Pair<Integer, Integer>> zoneSensorsCoords;

    public CityZone(final String idZone, final int x, final int y, final int xOffset, final int yOffset, final int index) {
        this.idZone = idZone;
        this.x = x;
        this.y = y;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.index = index;
        this.zoneSensorsCoords = new HashMap<>();
    }

    public String getIdZone() {
        return this.idZone;
    }

    public int getIndex() {
        return index;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getxOffset() {
        return xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public void addSensor(final String zoneSensorID, final Pair<Integer, Integer> coords) {
        this.zoneSensorsCoords.putIfAbsent(zoneSensorID, coords);
    }

    public Map<String, Pair<Integer, Integer>> getSensors() {
        return Collections.unmodifiableMap(this.zoneSensorsCoords);
    }

    @Override
    public String toString() {
        return "CityZone{" +
                "idZone='" + idZone + '\'' +
                ", index=" + index +
                ", x=" + x +
                ", y=" + y +
                ", xOffset=" + xOffset +
                ", yOffset=" + yOffset +
                ", zoneSensorsCoords=" + zoneSensorsCoords +
                '}';
    }
}
