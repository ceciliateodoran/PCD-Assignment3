package ass01.conc.patterns;

public interface IBarrier extends IMonitor{
	void hitAndWaitAll() throws InterruptedException;
	void evaluateSynchronize() throws InterruptedException;
}
