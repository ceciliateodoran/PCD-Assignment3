/*
 *   V2d.java
 *
 * Copyright 2000-2001-2002  aliCE team at deis.unibo.it
 *
 * This software is the proprietary information of deis.unibo.it
 * Use is subject to license terms.
 *
 */
package actor.utils;

/**
 *
 * 2-dimensional vector
 * objects are completely state-less
 *
 */
public class V2d  {

    public double x,y;

    public V2d(final double x, final double y){
        this.x = x;
        this.y = y;
    }

    public V2d(final V2d v){
        this.x = v.x;
        this.y = v.y;
    }

    public V2d(final P2d from, final P2d to){
        this.x = to.getX() - from.getX();
        this.y = to.getY() - from.getY();
    }

    public V2d scalarMul(final double k) {
    	x *= k;
    	y *= k;
    	return this;
    }
    
    public V2d sum(final V2d v) {
    	x += v.x;
    	y += v.y;
    	return this;
    }
    
    public V2d normalize() throws NullVectorException {
    	double mod =  Math.sqrt(x*x + y*y);
    	if (mod > 0) {
    		x /= mod;
    		y /= mod;
    		return this;
    	} else {
    		throw new NullVectorException();
    	}

    }
    public V2d change(final double x, final double y) {
    	this.x = x;
    	this.y = y;
    	return this;
    }
    
    public double getX() {
    	return x;
    }

    public double getY() {
    	return y;
    }
    
    
}
