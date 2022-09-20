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

	private JTextField totSensors;

	private JPanel mainPanel;

	private ActorRef<ViewMsg> viewActorRef;

    public VisualizerFrame(final int myArea, final int totAreas, final int w, final int h, final ActorRef<ViewMsg> viewActorRef){
        setTitle("Monitoring GUI");
        setSize(w,h);
        setResizable(false);

		this.viewActorRef = viewActorRef;

		this.createAreaPanel(myArea, totAreas);

		setContentPane(this.mainPanel);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    public void display(){
    	try {
			SwingUtilities.invokeAndWait(() -> {
				//updateAreaState();
				repaint();
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
		}
    }
    
    public void updateSimulationState(final String state) {

    	this.areaState.setText(state);

		if(state.equals("ALLARME")){
			this.managementButton.setText("GESTISCI");
			this.managementButton.setVisible(true);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();

		if (cmd.equals("GESTISCI")) {
			this.managementButton.setText("FINE GESTIONE");

			this.areaState.setText("IN GESTIONE");
			this.fireStationState.setText("OCCUPATA");

			// invio del messaggio al ViewActor
			this.viewActorRef.tell(new ViewUnderMngmtMsg());
		} else if (cmd.equals("FINE GESTIONE")) {
			this.managementButton.setText("");
			this.managementButton.setVisible(false);

			this.areaState.setText("OK");
			this.fireStationState.setText("LIBERA");

			// invio del messaggio al ViewActor
			this.viewActorRef.tell(new ViewEndMngmtMsg());
		}
	}

	private void createAreaPanel(final int myArea, final int tot){

		mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

		JPanel secondMainPanel = new JPanel();
		secondMainPanel.setLayout(new BoxLayout(secondMainPanel, BoxLayout.Y_AXIS));

		JScrollPane jscroll = new JScrollPane(secondMainPanel);
		mainPanel.add(jscroll);

		for (int i = 1; i <= tot; i++) {
			JPanel titlePanel = new JPanel();
			JLabel label = new JLabel("ZONA "+i);
			if(i == myArea){
				label.setForeground(Color.MAGENTA);
			}
			titlePanel.add(label);

			JPanel areaPanel = new JPanel();
			areaPanel.add(new JLabel("Stato ZONA "+i));
			if(i == myArea){
				this.areaState = new JTextField(20);
				this.areaState.setText("OK");
				this.areaState.setEditable(false);

				areaPanel.add(this.areaState);
			} else{
				JTextField areaState = new JTextField(20);
				areaState.setText("OK");
				areaState.setEditable(false);

				areaPanel.add(areaState);
			}

			JPanel fireStationPanel = new JPanel();
			fireStationPanel.add(new JLabel("Stato CASERMA "+i));

			if(i == myArea){
				this.fireStationState = new JTextField(20);
				this.fireStationState.setText("LIBERA");
				this.fireStationState.setEditable(false);

				fireStationPanel.add(this.fireStationState);
			}else {
				JTextField fireStationState = new JTextField(20);
				fireStationState.setText("LIBERA");
				fireStationState.setEditable(false);

				fireStationPanel.add(fireStationState);
			}

			JPanel sensorsPanel = new JPanel();
			JTextField nSensors = new JTextField(10);
			nSensors.setText("100");
			nSensors.setEditable(false);

			sensorsPanel.add(new JLabel("# sensori"));
			sensorsPanel.add(nSensors);

			JPanel cp = new JPanel();
			cp.setLayout(new BorderLayout());
			cp.add(BorderLayout.NORTH, titlePanel);
			cp.add(BorderLayout.WEST, fireStationPanel);
			cp.add(BorderLayout.CENTER, areaPanel);
			cp.add(BorderLayout.EAST, sensorsPanel);

			if(i == myArea){
				this.managementButton = new JButton("GESTISCI");
				this.managementButton.setVisible(true);

				cp.add(BorderLayout.SOUTH, this.managementButton);
				this.managementButton.addActionListener(this);
			}

			secondMainPanel.add(Box.createVerticalStrut(20));
			secondMainPanel.add(cp);
			secondMainPanel.add(Box.createVerticalStrut(20));

			if( i != tot ){
				secondMainPanel.add(Box.createVerticalStrut(20));
				secondMainPanel.add(new JSeparator());
			}
		}
	}

}
