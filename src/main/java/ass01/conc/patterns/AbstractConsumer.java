package ass01.conc.patterns;

public abstract class AbstractConsumer<Item,  M extends IProducerConsumer<Item>> extends Thread{
 
	protected final M monitor;
	public AbstractConsumer(M m){
		this.monitor = m;
	}
	
	public abstract void consume(Item item);
}
