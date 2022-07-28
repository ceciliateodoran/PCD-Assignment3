package ass01.conc.patterns;

import java.util.Optional;

public interface IProducerConsumer<Item> extends IMonitor{
	void put(Item item) throws InterruptedException;
    Optional<Item> get() throws InterruptedException;
}
