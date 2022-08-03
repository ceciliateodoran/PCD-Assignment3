package actor.view;

import actor.ViewMsg;
import actor.ViewStartMsg;
import actor.ViewStopMsg;
import actor.utils.Body;
import actor.utils.Boundary;
import akka.actor.typed.ActorRef;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class VisualizerFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JButton startButton;

	private JButton stopButton;

	private JTextField state;

	private VisualizerPanel panel;

	private ActorRef<ViewMsg> viewActorRef;

    public VisualizerFrame(final int w, final int h, final ActorRef<ViewMsg> viewActorRef){
        setTitle("Bodies Simulation");
        setSize(w,h);
        setResizable(false);

		this.viewActorRef = viewActorRef;
        
        this.startButton = new JButton("start");
        this.stopButton = new JButton("stop");
        this.stopButton.setEnabled(false);
        
        JPanel controlPanel = new JPanel();
		controlPanel.add(this.startButton);
		controlPanel.add(this.stopButton);

		this.panel = new VisualizerPanel(w,h);
        
        JPanel infoPanel = new JPanel();
		this.state = new JTextField(20);
		this.state.setText("Idle");
		this.state.setEditable(false);
		infoPanel.add(new JLabel("State"));
		infoPanel.add(this.state);
		JPanel cp = new JPanel();
		LayoutManager layout = new BorderLayout();
		cp.setLayout(layout);
		cp.add(BorderLayout.NORTH, controlPanel);
		cp.add(BorderLayout.CENTER, this.panel);
		cp.add(BorderLayout.SOUTH, infoPanel);
		setContentPane(cp);

		this.startButton.addActionListener(this);
		this.stopButton.addActionListener(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    public void display(final List<Body> bodies, final double vt, final long iter, final Boundary bounds){
    	try {
			SwingUtilities.invokeAndWait(() -> {
				this.panel.display(bodies, vt, iter, bounds);
				repaint();
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
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
			this.state.setText("Running");
			// invio del messaggio di Start al ViewActor
			this.viewActorRef.tell(new ViewStartMsg());
		} else if (cmd.equals("stop")) {
			switchButtons();
			this.state.setText("Stopped");
			// invio del messaggio di Stop al ViewActor
			this.viewActorRef.tell(new ViewStopMsg());
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
