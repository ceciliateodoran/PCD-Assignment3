package ass01.view;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ViewMonitor {
	private Lock mutex;
	private Condition isStarted;
	public boolean toReset;
	
	public ViewMonitor() {
		this.mutex = new ReentrantLock();
		this.isStarted = mutex.newCondition();
		this.toReset = false;
	}
	
	public void waitStart() throws InterruptedException {
		try {
			mutex.lock();
			this.toReset = false;
			isStarted.await();
		} finally {
			mutex.unlock();
		}
	}
	
	public void start() {
		try {
			mutex.lock();
			isStarted.signal();
		} finally {
			mutex.unlock();
		}
	}
	
	public boolean evaluateReset() {
		return this.toReset;
	}
	
	public void stop() {
		this.toReset = true;
	}
}
