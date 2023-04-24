package distributed.utils;

import akka.japi.Pair;
import distributed.City;
import distributed.CityZone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CalculatorZone {
    private static final String DEFAULT_SENSOR_NAME = "Sensor";
    private static final String DEFAULT_COORDINATOR_NAME = "ZoneCoordinator";
    private City city;

    public CalculatorZone(final City c) {
        this.city = c;
    }

    public List<Pair<Integer, Integer>> setZoneSensors() {
        int z = 1, j = 0;
        int nZones = (this.city.getGridColumns() + 1) * (this.city.getGridRows() + 1);
        int nSensors = ((int) Math.ceil(this.city.getSensors() / nZones));
        List<Pair<Integer, Integer>> sensorsInZones = new ArrayList<>();

        for (int i = 0; i < this.city.getSensors(); i++) {
            sensorsInZones.add(new Pair<>(i, z));
            j++;
            if (j == nSensors && z < nZones) {
                j = 0;
                z++;
            }
        }

        return sensorsInZones;
    }

    public List<CityZone> defineZones() {
        int x, y, index = 0;
        int xOffset = this.city.getWidth() / (this.city.getGridColumns() + 1);
        int yOffset = this.city.getHeight() / (this.city.getGridRows() + 1);
        List<CityZone> cityZones = new ArrayList<>();

        for (int i = 0; i < this.city.getGridRows(); i++) {
            for (int j = 0; j < this.city.getGridColumns(); j++) {
                x = xOffset * j;
                y = yOffset * i;
                index++;
                cityZones.add(new CityZone(DEFAULT_COORDINATOR_NAME + index, x, y, xOffset, yOffset, index));
            }
        }

        return cityZones;
    }

    public List<CityZone> setSensorsInZones() {
        List<Pair<Integer, Integer>> sensorsInZones = this.setZoneSensors();
        List<CityZone> cityZones = this.defineZones();

        for (final CityZone zone : cityZones) {
            for (final Pair<Integer, Integer> sensorZonePair : sensorsInZones) {
                if (zone.getIndex() == sensorZonePair.second()) {
                    Pair<Integer, Integer> sensorsCoordsInZone = new Pair<>(new Random(zone.getX()).nextInt( zone.getX() + zone.getxOffset()),
                            new Random(zone.getY()).nextInt( zone.getY() + zone.getyOffset()));
                    zone.addSensor(DEFAULT_SENSOR_NAME + sensorZonePair.first().toString(), sensorsCoordsInZone);
                }
            }
        }

        return cityZones;
    }
}
