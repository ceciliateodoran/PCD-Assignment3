package ass01.seq;

import java.util.ArrayList;

import ass01.utils.Body;
import ass01.utils.BodyGenerator;
import ass01.utils.Boundary;

/**
 * Bodies simulation - legacy code: sequential, unstructured
 */
public class SequentialBodySimulationMain{

    public static void main(String[] args) {
        Boundary bounds = new Boundary(-6.0, -6.0, 6.0, 6.0);
        BodyGenerator bg = new BodyGenerator();
        ArrayList<Body> bodies = bg.generateBodies(0, bounds);

    	SequentialSimulator sim = new SequentialSimulator(bodies, bounds);

        sim.execute(1000);
    }
}
