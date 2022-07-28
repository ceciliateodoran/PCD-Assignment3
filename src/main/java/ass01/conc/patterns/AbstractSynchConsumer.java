package ass01.conc.patterns;

import java.util.Optional;

public abstract class AbstractSynchConsumer<Item, M extends IProducerConsumer<Item> & IBarrier> 
					  extends AbstractConsumer<Item, M>{

	protected int iter = 0;
	public AbstractSynchConsumer(M monitor) {
		super(monitor);
	}
	
	public void run(){
	    //loop forever in search to produced items in the monitor's buffer to "consume" (AKA to update pos and check collision)
		while (true){
			try {
				Optional<Item> item = monitor.get();
				if(item.isPresent()) consume(item.get());
				monitor.evaluateSynchronize();
			} catch (InterruptedException ex){
				ex.printStackTrace();
			}
			
		}
	}
}
