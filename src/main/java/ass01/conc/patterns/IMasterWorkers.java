package ass01.conc.patterns;

import java.util.ArrayList;
import java.util.Map;


public interface IMasterWorkers<Item> extends IMonitor{
	void synchMasterWorker() throws InterruptedException;
	void startAndWaitWorkers(ArrayList<Item> rol) throws InterruptedException;
	Map<String, ? extends Object> initializeWorkerResources(Object[] args);
}
