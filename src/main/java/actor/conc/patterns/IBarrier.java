package actor.conc.patterns;

public interface IBarrier extends IMonitor{
	void hitAndWaitAll() throws InterruptedException;
	void evaluateSynchronize() throws InterruptedException;
}
