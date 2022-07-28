package ass01.conc.patterns;

public abstract class AbstractSCWithMaster<Item, M extends IProducerConsumer<Item> & IMasterWorkers<Item> & IBarrier> 
					  extends AbstractSynchConsumer<Item, M> {

	public AbstractSCWithMaster(M monitor) {
		super(monitor);
	}
}
