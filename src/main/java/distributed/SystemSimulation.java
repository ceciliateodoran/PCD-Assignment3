package distributed;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SystemSimulation {
    public static void main(String[] args) {
        int cityRows = 2;
        int cityColumns = 1;
        int cityWidth = 400;
        int cityHeight = 200;
        Map<String, Integer> cityDimensions = Map.of(
                "rows", cityRows,
                "columns", cityColumns,
                "width", cityWidth,
                "height", cityHeight
        );

        City city = new City(cityWidth, cityHeight, cityRows, cityColumns, 5, 100);
        if (args.length == 0) {
            Builder root = new Builder(city);
            root.startup(2440);
            root.addGuiActor(2440, 0, cityDimensions);
        } else {
            Arrays.stream(args).map(Integer::parseInt).forEach(Builder::startup);
        }
    }
}
