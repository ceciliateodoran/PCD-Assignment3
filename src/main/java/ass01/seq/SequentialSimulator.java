package ass01.seq;

import java.util.*;

import actor.utils.*;
import ass01.utils.AbstractSimulator;
import ass01.utils.Body;
import ass01.utils.Boundary;
import ass01.utils.V2d;

public class SequentialSimulator extends AbstractSimulator {

	public SequentialSimulator(ArrayList<Body> bodies, Boundary bounds) {
		super(bodies, bounds);
	}

	public void execute(final long nSteps) {

		/* simulation loop */
		while (iter < nSteps) {
			/* update bodies velocity */
			for (int i = 0; i < bodies.size(); i++) {
				Body b = bodies.get(i);

				/* compute total force on bodies */
				V2d totalForce = computeTotalForceOnBody(b);
				
				/* compute instant acceleration */
				V2d acc = new V2d(totalForce).scalarMul(1.0 / b.getMass());

				/* update velocity */
				b.updateVelocity(acc, dt);
			}

			/* compute bodies new pos */
			for (Body b : bodies) {
				b.updatePos(dt);
				
			}

			/* check collisions with boundaries */
			for (Body b : bodies) {
				b.checkAndSolveBoundaryCollision(bounds);
			}

			/* update virtual time */
			vt = vt + dt;
			iter++;
		}
	}

	public ArrayList<Body> getBodies() {
		return this.bodies;
	}

	private V2d computeTotalForceOnBody(final Body b) {

		V2d totalForce = new V2d(0, 0);

		/* compute total repulsive force */
		for (int j = 0; j < bodies.size(); j++) {
			Body otherBody = bodies.get(j);
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
