package distributed;

import distributed.utils.Pair;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the data structure of a city zone
 */
public class CityZone {
    private String idZone;
    private int index;
    private int x;
    private int y;
    private int xOffset;
    private int yOffset;
    private Map<String, Pair<Integer, Integer>> zoneSensorsCoords;

    /**
     * Construct a new instance of the city zone
     *
     * @param idZone The zone identifier
     * @param x The x coordinate
     * @param y The y coordinate
     * @param xOffset The x offset
     * @param yOffset The y offset
     * @param index The index of the city zone
     */
    public CityZone(final String idZone, final int x, final int y, final int xOffset, final int yOffset, final int index) {
        this.idZone = idZone;
        this.x = x;
        this.y = y;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.index = index;
        this.zoneSensorsCoords = new HashMap<>();
    }

    /**
     * @return the zone identifier value
     */
    public String getIdZone() {
        return this.idZone;
    }

    /**
     * @return the city zone index value
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the x coordinate value
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y coordinate value
     */
    public int getY() {
        return y;
    }

    /**
     * @return the x offset value
     */
    public int getxOffset() {
        return xOffset;
    }

    /**
     * @return the y offset value
     */
    public int getyOffset() {
        return yOffset;
    }

    /**
     * It adds a new sensor with an identifier and the corresponding coordinates
     *
     * @param zoneSensorID The identifier of the sensor in the city zone
     * @param coords The coordinates of the sensor
     */
    public void addSensor(final String zoneSensorID, final Pair<Integer, Integer> coords) {
        this.zoneSensorsCoords.put(zoneSensorID, coords);
    }

    /**
     * @return all the sensors in the city zone
     */
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
