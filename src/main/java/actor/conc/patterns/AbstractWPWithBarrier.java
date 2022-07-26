package actor.conc.patterns;

public abstract class AbstractWPWithBarrier<Item, M extends IProducerConsumer<Item> & IMasterWorkers<Item> & IBarrier> 
				extends AbstractWorkerProducer<Item, M> {

	public AbstractWPWithBarrier(M monitorMW) {
		super(monitorMW);
	}

}
