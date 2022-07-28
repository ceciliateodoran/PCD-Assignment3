package ass01.view;

public class Controller implements ActionListener {
	
	public ViewMonitor m;
	
	public Controller() {
		this.m = new ViewMonitor();
	}

	@Override
	public void started() {
		this.m.start();
	}

	@Override
	public void stopped() {
		this.m.stop();
		//this.stopFlag.set();
	}
}
