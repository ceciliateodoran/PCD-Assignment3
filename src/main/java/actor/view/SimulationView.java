package actor.view;

import actor.message.ViewMsg;
import actor.utils.Body;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Manager of the graphic user interface
 */
public class SimulationView {
        
	private VisualizerFrame frame;

    public SimulationView(final int w, final int h, final ActorRef<ViewMsg> viewActorRef){
    	this.frame = new VisualizerFrame(w, h, viewActorRef);
    }
        
    public void updateView(final List<Body> bodies, final double vt, final long iter, final Boundary bounds){
    	this.frame.display(bodies, vt, iter, bounds);
    }
    
    public void updateState(final String state) {
    	this.frame.updateSimulationState(state);
    }
    
    public void display() {
        try {
			SwingUtilities.invokeAndWait(() -> {
				this.frame.setVisible(true);
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
    }
}