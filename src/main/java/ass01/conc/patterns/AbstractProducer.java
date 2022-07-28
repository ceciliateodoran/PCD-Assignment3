package ass01.conc.patterns;

import ass01.utils.NotImplementedException;

public abstract class AbstractProducer<Item, M extends IProducerConsumer<Item>> extends Thread {

	protected final M monitor;
	public AbstractProducer(M m){
		this.monitor = m;
	}

	public abstract Item produce() throws NotImplementedException;
	public abstract Item produce(Object[] args) throws NotImplementedException;
	
	
}
