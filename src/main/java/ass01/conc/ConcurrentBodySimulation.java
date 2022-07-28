package ass01.conc;

import java.util.ArrayList;

import ass01.utils.Body;
import ass01.utils.BodyGenerator;
import ass01.utils.Boundary;
import ass01.utils.SimulationView;
import ass01.view.Controller;

/**
 * Bodies simulation - legacy code: concurrent
 * 
 */
public class ConcurrentBodySimulation {
	
	public static void main(String[] args) throws InterruptedException {
		
		Boundary bounds =  new Boundary(-6.0, -6.0, 6.0, 6.0);
		BodyGenerator bg = new BodyGenerator();
		ArrayList<Body> bodies = bg.generateBodies(1000, bounds);
		
		SimulationView viewer = new SimulationView(620,620);
		
		Controller controller = new Controller();

    	ConcurrentSimulator sim = new ConcurrentSimulator(viewer, controller, bodies, bounds);

        viewer.addListener(controller);
        viewer.display();
        sim.execute(10000);
    }
}
