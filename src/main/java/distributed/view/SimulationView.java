package distributed.view;

import akka.actor.typed.ActorRef;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class SimulationView {
        
	private VisualizerFrame frame;

    public SimulationView(final int myArea, final int totAreas, final int w, final int h, final ActorRef<ViewMsg> viewActorRef){
    	this.frame = new VisualizerFrame(myArea, totAreas, w, h, viewActorRef);
    }
        
    public void updateView(){
    	this.frame.display();
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