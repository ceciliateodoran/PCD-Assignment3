package distributed.utils;

import distributed.City;
import distributed.CityZone;

import java.util.*;

/**
 * Represents the implementation of the calculator zone:
 * starting with the size of a city, it calculates the number of zones into which it can be divided
 * and assigns the same number of sensors to each one
 */
public class CalculatorZone {
    private static final String DEFAULT_SENSOR_NAME = "Sensor";
    private static final String DEFAULT_BARRACK_ZONE_NAME = "BarrackZone";
    private static final String BARRACK_ZONE_HOSTNAME = "127.0.0.3";
    private static final String GUI_ZONE_NAME = "127.1.0.2";
    private static final int DEFAULT_SENSORS_PORT = 2660;
    private static final int DEFAULT_BARRACK_ZONE_PORT = 2870;
    private static final int DEFAULT_GUI_ZONE_PORT = 2200;
    private City city;
    private ClusterStructure clusterStructure;

    /**
     * Construct a new instance of the CalculatorZone
     *
     * @param city The city to consider for the calculation
     * @param clusterStructure The cluster structure to create during the calculation
     */
    public CalculatorZone(final City city, final ClusterStructure clusterStructure) {
        this.city = city;
        this.clusterStructure = clusterStructure;
    }

    private List<Pair<Integer, Integer>> setZoneSensors() {
        int z = 0, j = 0, indSensor = 0;
        int nZones = (this.city.getGridColumns() + 1) * (this.city.getGridRows() + 1);
        int nSensors = this.city.getSensors();
        List<Pair<Integer, Integer>> sensorsInZones = new ArrayList<>();

        for (int i = 0; i < this.city.getSensors() * nZones; i++) {
            sensorsInZones.add(new Pair<>(indSensor, z));
            clusterStructure.addPhysicalHostSensor(new Pair<>(indSensor, z), new Pair<>("127.0.0." + (i + 10), DEFAULT_SENSORS_PORT + i));
            j++;
            indSensor++;
            if (j % nSensors == 0 && z < nZones) {
                clusterStructure.setSensorPerZone(indSensor);
                indSensor = 0;
                j = 0;
                z++;
            }
        }

        return sensorsInZones;
    }

    private List<CityZone> defineZones() {
        int x, y, index = 0;
        int xOffset = this.city.getWidth() / (this.city.getGridColumns() == 0 ? 1 : this.city.getGridColumns());
        int yOffset = this.city.getHeight() / (this.city.getGridRows() == 0 ? 1 : this.city.getGridRows());
        List<CityZone> cityZones = new ArrayList<>();

        for (int i = 0; i < this.city.getGridRows(); i++) {
            for (int j = 0; j < this.city.getGridColumns(); j++) {
                x = xOffset * j;
                y = yOffset * i;
                cityZones.add(new CityZone(DEFAULT_BARRACK_ZONE_NAME + index, x, y, xOffset, yOffset, index));
                clusterStructure.addPhysicalHostBarracksZone(index, new Pair<>(BARRACK_ZONE_HOSTNAME, DEFAULT_BARRACK_ZONE_PORT + index));
                clusterStructure.addPhysicalGuiSystem(index, new Pair<>(GUI_ZONE_NAME, DEFAULT_GUI_ZONE_PORT + index));
                index++;
            }
        }
        return cityZones;
    }

    /**
     * @return the list of created zones of the city
     */
    public List<CityZone> setSensorsInZones() {
        List<Pair<Integer, Integer>> sensorsInZones = this.setZoneSensors();
        List<CityZone> cityZones = this.defineZones();

        for (final CityZone zone : cityZones) {
            for (final Pair<Integer, Integer> sensorZonePair : sensorsInZones) {
                if (zone.getIndex() == sensorZonePair.second()) {
                    int x = new Random().nextInt((zone.getX() + zone.getxOffset()) - zone.getX() + 1) + zone.getX();
                    int y = new Random().nextInt((zone.getY() + zone.getyOffset()) - zone.getY() + 1) + zone.getY();
                    Pair<Integer, Integer> sensorsCoordsInZone = new Pair<>(x, y);
                    zone.addSensor(DEFAULT_SENSOR_NAME + sensorZonePair.first().toString(), sensorsCoordsInZone);
                }
            }
        }
        return cityZones;
    }
}
