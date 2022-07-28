package ass01.conc.patterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ass01.utils.NotImplementedException;

public abstract class AbstractWorkerProducer<Item, M extends IProducerConsumer<Item> & IMasterWorkers<Item>> 
					  extends AbstractProducer<Item,  M>{

	private List<Item> toProduce;
	protected Map<String, ? extends Object> resources;
	private Object[] args;
	
	public AbstractWorkerProducer(M monitorMW) {
		super(monitorMW);
		this.args = new Object[0];
		this.toProduce = new ArrayList<>();
	}
	
	public void run(){
	    while(true) {
	    	try {
				this.monitor.synchMasterWorker();
				this.resources = monitor.initializeWorkerResources(args);
		    	manageResources();
		    	
	    		//for each body in toProduce put in the monitor's buffer the updated body
		        for(Item i:toProduce){
		        	Object[] temp = new Object[1];
		        	temp[0] = i;
		        	i = produce(temp);
	            	monitor.put(i);
		        }
			} catch (InterruptedException | NotImplementedException e) {
				e.printStackTrace();
			}
	    }
    }
	
	public void addResourceInitParameter(Object o) {
		List<Object> temp = new ArrayList<>();
		for(int i = 0; i < this.args.length; i++) {
			temp.add(this.args[i]);
		}
		
		temp.add(o);
		this.args = temp.toArray();
	}
	
	public void assignTask(List<Item> workerSublist) {
		this.toProduce.clear();
		for(Item elem: workerSublist) {
			this.toProduce.add(elem);
		}
	}
	
	protected abstract void manageResources();
}
