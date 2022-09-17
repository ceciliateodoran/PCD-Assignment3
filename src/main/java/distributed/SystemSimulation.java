package distributed;

import java.util.Arrays;

public class SystemSimulation {
    public static void main(String[] args) {
        City city = new City(400, 200, 2, 3, 12);
        if (args.length == 0) {
            Root root = new Root(city);
            root.startup(2440);
        } else
            Arrays.stream(args).map(Integer::parseInt).forEach(Root::startup);
    }
}
