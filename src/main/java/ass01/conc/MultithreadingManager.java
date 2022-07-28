package ass01.conc;

import java.util.ArrayList;

import ass01.conc.patterns.SynchronizedPipelineMonitor;
import ass01.utils.Body;
import ass01.utils.Boundary;

public class MultithreadingManager {
	
	private SynchronizedPipelineMonitor<Body> monitor;

	/* boundary of the field */
	private Boundary bounds;

	/* virtual time step */
	double dt;
	
	/* number of producers in producers-consumers pattern*/
	private final int nrVelCalculators;
	
	private final int nrPosCalculators;
	
	/* number of total process available*/
	private final int nrProcessors;
	
	/* size of the sublists given the number of producers */
	private final int deltaSplitList;
	
	/* number of elements to assign to the last producer (equals to nrProcessors % size total list)*/
	private final int restSplitList;
	
	/*Lists of producers and consumers*/
	private ArrayList<VelCalculator> velCalculators;
	private ArrayList<PosCalculator> posCalculators;
	
	private final static double VC_PERCENTAGE = (7.0/10.0);

	public MultithreadingManager(final ArrayList<Body> bodies, final Boundary bounds, final double dt) {
		
		this.bounds = bounds;
		this.dt = dt;
	
		this.nrProcessors = Runtime.getRuntime().availableProcessors() + 1;
		this.nrVelCalculators =  nrProcessors >= bodies.size() ? 
					   bodies.size() : 
					   (int)(VC_PERCENTAGE * (nrProcessors));
		
		this.nrPosCalculators = this.nrProcessors - this.nrVelCalculators;
		
		this.deltaSplitList = (int) Math.ceil((float) (bodies.size() / nrVelCalculators));
		this.restSplitList = bodies.size() % nrVelCalculators;
		this.posCalculators = new ArrayList<>();
		this.velCalculators = new ArrayList<>();
		this.monitor = new SynchronizedPipelineMonitor<>(nrVelCalculators + 1, nrPosCalculators, bodies);
		
		//initialize consumers: they will remain alive the whole time
		this.initialize_velocity_calculators();	
		this.initialize_position_calculators();
	}
	
	public SynchronizedPipelineMonitor<Body> getMonitor(){
		return this.monitor;
	}
	
	private void initialize_position_calculators() {
		for(int i = 0; i < this.nrPosCalculators; i++) {
		    PosCalculator pc = new PosCalculator(monitor, dt, bounds);
			pc.start();
			this.posCalculators.add(pc);
		}
	}
	
	private void initialize_velocity_calculators() {
		int fromIndex, toIndex;
        
		for(int i = 0; i<nrVelCalculators; i++) {
			fromIndex = i * deltaSplitList;
			toIndex = (i + 1) * deltaSplitList + (i == nrVelCalculators-1 ? restSplitList : 0);
			VelCalculator vc = new VelCalculator(this.monitor, fromIndex, toIndex, this.dt);
			vc.start();
			this.velCalculators.add(vc);
		}
	}
}
