package distributed.utils;

import akka.japi.Pair;
import distributed.City;

import java.util.ArrayList;
import java.util.List;

public class CalculatorZone {
    private City city;

    public CalculatorZone(final City c) {
        this.city = c;
    }

    public List<Pair<Integer, Integer>> setZoneSensors() {
        int z = 1, j = 0;
        int nZones = this.city.getGridColumns() * this.city.getGridRows();
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
}
