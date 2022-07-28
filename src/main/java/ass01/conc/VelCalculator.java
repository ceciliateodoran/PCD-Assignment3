package ass01.conc;

import java.util.List;

import ass01.conc.patterns.AbstractWPWithBarrier;
import ass01.conc.patterns.SynchronizedPipelineMonitor;
import ass01.utils.Body;
import ass01.utils.NotImplementedException;
import ass01.utils.V2d;

public class VelCalculator extends AbstractWPWithBarrier<Body, SynchronizedPipelineMonitor<Body>> {

	//the total list of bodies of the simulation
    private List<Body> bodiesView;
    
    private final double dt;
    
	public VelCalculator(SynchronizedPipelineMonitor<Body> monitorMW, int from, int to, double dt) {
		super(monitorMW);
		this.dt = dt;
		this.addResourceInitParameter(from);
		this.addResourceInitParameter(to);
	}

	@Override
	public Body produce() throws NotImplementedException {
		throw new NotImplementedException();
	}

	@Override
	public Body produce(Object[] args) {
		Body item = (Body) args[0];
		 /* compute total force on bodies */
        V2d totalForce = computeTotalForceOnBody(item);
        
        /* compute instant acceleration */
        V2d acc = new V2d(totalForce).scalarMul(1.0 / item.getMass());

        /* update velocity */
        item.updateVelocity(acc, dt);
        return item;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void manageResources() {
		this.bodiesView = (List<Body>) resources.get("bodiesView");
		this.assignTask((List<Body>) resources.get("toProduce"));
	}
	
	 private V2d computeTotalForceOnBody(Body b) {
	        V2d totalForce = new V2d(0, 0);
	     
	        /* compute total repulsive force */
	        for (int j = 0; j < this.bodiesView.size(); j++) {
	        	Body otherBody = this.bodiesView.get(j);
	            if (!b.equals(otherBody)) {
	            	try {
	            		V2d forceByOtherBody = b.computeRepulsiveForceBy(otherBody);
	                    totalForce.sum(forceByOtherBody);
	                } catch (Exception ex) { 
	                	
	                }
	            }
	        }
	        /* add friction force */
	        totalForce.sum(b.getCurrentFrictionForce());
	        return totalForce;
	 }

}
