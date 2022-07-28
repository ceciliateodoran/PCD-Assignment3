package ass01.utils;

import java.util.ArrayList;
import java.util.Iterator;


public abstract class AbstractSimulator {
	protected final ArrayList<Body> bodies;
	
	protected ArrayList<Body> initialBodies;
	
	/* virtual time */
	protected double vt;

	/* virtual time step */
	protected final double dt;
	
	/* boundary of the field */
	protected final Boundary bounds;
	
	protected long iter;
	
	public AbstractSimulator(final ArrayList<Body> bodies, final Boundary bounds){
		/* init virtual time */
		this.dt = 0.001;
		this.vt = 0;
		this.iter = 0;
		this.bounds = bounds;
		this.bodies = new ArrayList<>();
		this.initialBodies = new ArrayList<>();
		
		this.copyAndReplace(bodies, this.bodies);
		this.copyAndReplace(this.bodies, this.initialBodies);
	}
	
	protected void copyAndReplace(final ArrayList<Body> source, final ArrayList<Body> destination) {
		destination.clear();
		Iterator<Body> iterator = source.iterator();
        while(iterator.hasNext()){
            destination.add((Body)iterator.next().clone());
        }
	}
	
	protected void reset() {
		this.copyAndReplace(this.initialBodies, this.bodies);
		this.vt = 0;
		this.iter = 0;
	}
	
	public abstract void execute(final long nSteps);
}


