package actor.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Random;

/**
 * Generator of all Bodies inside given boundaries
 *
 */
public class BodyGenerator {
	
	public ArrayList<Body> generateBodies(final int nBodies, final Boundary bounds) {
		ArrayList<Body> bodies = new ArrayList<>();
		Random rand = new Random(System.currentTimeMillis());
		bodies = new ArrayList<Body>();
		for (int i = 0; i < nBodies; i++) {
			double x = bounds.getX0()*0.25 + rand.nextDouble() * (bounds.getX1() - bounds.getX0()) * 0.25;
			double y = bounds.getY0()*0.25 + rand.nextDouble() * (bounds.getY1() - bounds.getY0()) * 0.25;
			Body b = new Body(i, new P2d(x, y), new V2d(0, 0), 10);
			bodies.add(b);
		}
		return bodies;
	}
	
	public static double round(final double value, final int places) {
	    if (places < 0) throw new IllegalArgumentException();
	    return new BigDecimal(value).setScale(places, RoundingMode.HALF_UP).doubleValue();
	}

}
