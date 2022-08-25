package distributed.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ConcurrentModificationException;
import java.util.List;

public class VisualizerPanel extends JPanel implements KeyListener {
    
	private static final long serialVersionUID = 1L;

	private double scale = 1;
	
    private long dx;

    private long dy;
    
    public VisualizerPanel(int w, int h){
        setSize(w, h);
        dx = w/2 - 20;
        dy = h/2 - 20;
		this.addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
		requestFocusInWindow(); 
    }

    public void paint(Graphics g){    		    		

    }
    
    private int getXcoord(double x) {
    	return (int)(dx + x*dx*scale);
    }

    private int getYcoord(double y) {
    	return (int)(dy - y*dy*scale);
    }
    
    public void display(){

    }
    
    public void updateScale(double k) {
    	scale *= k;
    }

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == 38){  		/* KEY UP */
			scale *= 1.1;
		} else if (e.getKeyCode() == 40){  	/* KEY DOWN */
			scale *= 0.9;  
		} 
	}

	public void keyReleased(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}
