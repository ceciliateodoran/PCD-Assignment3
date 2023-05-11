package distributed;

import java.util.Arrays;

public class SystemSimulation {
    public static void main(String[] args) {
        City city = new City(400, 200, 1, 3, 5, 100);
        if (args.length == 0) {
            Deployer root = new Deployer(city);
            root.startup(2440);
        } else {
            Arrays.stream(args).map(Integer::parseInt).forEach(Deployer::startup);
        }
    }
}
