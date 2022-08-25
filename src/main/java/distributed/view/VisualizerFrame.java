package distributed.view;

import akka.actor.typed.ActorRef;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

public class VisualizerFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private JButton managementButton;

	private JTextField areaState;

	private JTextField fireStationState;

	private ActorRef<ViewMsg> viewActorRef;

    public VisualizerFrame(final int w, final int h, final ActorRef<ViewMsg> viewActorRef){
        setTitle("Monitoring GUI");
        setSize(w,h);
        setResizable(false);

		this.viewActorRef = viewActorRef;

		//pannello con info relative alla mia zona
		JPanel title1Panel = new JPanel();
		title1Panel.add(new JLabel("ZONA C"));
        
        JPanel area1Panel = new JPanel();
		this.areaState = new JTextField(20);
		this.areaState.setText("OK");
		this.areaState.setEditable(false);

		this.managementButton = new JButton("");
		this.managementButton.setVisible(false);

		area1Panel.add(new JLabel("Stato ZONA C"));
		area1Panel.add(this.areaState);
		area1Panel.add(Box.createHorizontalStrut(20));
		area1Panel.add(this.managementButton);

		JPanel fireStation1Panel = new JPanel();
		this.fireStationState = new JTextField(20);
		this.fireStationState.setText("LIBERA");
		this.fireStationState.setEditable(false);

		fireStation1Panel.add(new JLabel("Stato CASERMA C"));
		fireStation1Panel.add(this.fireStationState);

		JPanel controlPanel = new JPanel();
		controlPanel.add(this.managementButton);

		JPanel cp = new JPanel();
		cp.setLayout(new BorderLayout());
		cp.add(BorderLayout.NORTH, title1Panel);
		cp.add(BorderLayout.CENTER, area1Panel);
		cp.add(BorderLayout.SOUTH, fireStation1Panel);

		//pannello con info relative alla zona vicina in allarme
		JPanel title2Panel = new JPanel();
		title2Panel.add(new JLabel("ZONA A"));

		JPanel area2Panel = new JPanel();
		JTextField stateArea2 = new JTextField(20);
		stateArea2.setText("ALLARME");
		stateArea2.setEditable(false);

		area2Panel.add(new JLabel("Stato ZONA A"));
		area2Panel.add(stateArea2);

		JPanel fireStation2Panel = new JPanel();
		JTextField state2FireStation = new JTextField(20);
		state2FireStation.setText("OCCUPATA");
		state2FireStation.setEditable(false);

		fireStation2Panel.add(new JLabel("Stato CASERMA A"));
		fireStation2Panel.add(state2FireStation);

		JPanel sp = new JPanel();
		sp.setLayout(new BorderLayout());
		sp.add(BorderLayout.NORTH, title2Panel);
		sp.add(BorderLayout.CENTER, area2Panel);
		sp.add(BorderLayout.SOUTH, fireStation2Panel);

		//pannello principale
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		mainPanel.add(cp);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(sp);

		setContentPane(mainPanel);

		this.managementButton.addActionListener(this);
		setDefaultCloseOperation(EXIT_ON_CLOSE);

		// invio del messaggio di Start al ViewActor
		this.viewActorRef.tell(new ViewStartMsg());
    }
    
    public void display(){
    	try {
			SwingUtilities.invokeAndWait(() -> {
				repaint();
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    public void updateSimulationState(final String state) {
    	//switchButtons();
    	this.areaState.setText(state);
		if(state.equals("ALLARME") && this.fireStationState.getText().equals("LIBERA")){
			this.managementButton.setText("GESTISCI");
			this.managementButton.setVisible(true);
		} else if (state.equals("IN GESTIONE") && this.fireStationState.getText().equals("OCCUPATA")) {


		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("stop")) {
			//switchButtons();
			this.areaState.setText("Stopped");
			// invio del messaggio di Stop al ViewActor
			this.viewActorRef.tell(new ViewStopMsg());
		}
	}
	
	/*private void switchButtons() {
			this.stopButton.setEnabled(true);
		} else {

			this.stopButton.setEnabled(false);
		}
	}*/
}
