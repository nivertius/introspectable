package org.perfectable.introspection;

import com.google.common.base.Throwables;

public final class Threads {
	
	public interface ThrowingRunnable {
		void run() throws Throwable;
		
		default Runnable asRunnablePropagated(ThrowingRunnable code) {
			return () -> {
				try {
					code.run();
				}
				catch(Throwable e) { // NOPMD throwable caught intentionally
					Throwables.propagate(e);
				}
			};
		}
	}
	
	public static void startDaemon(Runnable code) {
		Thread thread = new Thread(code);
		thread.setDaemon(true);
		thread.start();
	}
	
	public static void addShutdownHook(Runnable code) {
		Runtime.getRuntime().addShutdownHook(new Thread(code));
	}
	
	public static void addPropagatedShutdownHook(ThrowingRunnable code) {
		addShutdownHook(code.asRunnablePropagated(code));
	}
	
	private Threads() {
		// utility class
	}
	
	public static void waitForInterrupt() {
		Object monitor = new Object();
		synchronized(monitor) {
			try {
				monitor.wait();
			}
			catch(InterruptedException e) {
				// return
			}
		}
	}
}
