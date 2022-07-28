package ass01.view;

import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ass01.utils.Body;
import ass01.utils.Boundary;

public class VisualizerFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JButton startButton;
	private JButton stopButton;
	private JTextField state;
	private VisualizerPanel panel;
	private ass01.view.ActionListener controller;

    public VisualizerFrame(int w, int h){
        setTitle("Bodies Simulation");
        setSize(w,h);
        setResizable(false);
        
        this.startButton = new JButton("start");
        this.stopButton = new JButton("stop");
        this.stopButton.setEnabled(false);
        
        JPanel controlPanel = new JPanel();
		controlPanel.add(startButton);
		controlPanel.add(stopButton);
		
        panel = new VisualizerPanel(w,h);
        
        JPanel infoPanel = new JPanel();
		state = new JTextField(20);
		state.setText("Idle");
		state.setEditable(false);
		infoPanel.add(new JLabel("State"));
		infoPanel.add(state);
		JPanel cp = new JPanel();
		LayoutManager layout = new BorderLayout();
		cp.setLayout(layout);
		cp.add(BorderLayout.NORTH, controlPanel);
		cp.add(BorderLayout.CENTER, panel);
		cp.add(BorderLayout.SOUTH, infoPanel);
		setContentPane(cp);	
		
		startButton.addActionListener(this);
		stopButton.addActionListener(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    public void display(ArrayList<Body> bodies, double vt, long iter, Boundary bounds){
    	try {
			SwingUtilities.invokeAndWait(() -> {
				panel.display(bodies, vt, iter, bounds);
				repaint();
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
    };
    
    public void addListener(ass01.view.ActionListener l) {
    	this.controller = l;
    }
    
    public void updateSimulationState(final String state) {
    	switchButtons();
    	this.state.setText(state);
    }

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if (cmd.equals("start")) {
			switchButtons();
			state.setText("Running");
			this.controller.started();
		} else if (cmd.equals("stop")) {
			switchButtons();
			state.setText("Stopped");
			this.controller.stopped();
		}
	}
	
	private void switchButtons() {
		if (this.startButton.isEnabled() && !this.stopButton.isEnabled()) {
			this.startButton.setEnabled(false);
			this.stopButton.setEnabled(true);
		} else {
			this.startButton.setEnabled(true);
			this.stopButton.setEnabled(false);
		}
	}
}
