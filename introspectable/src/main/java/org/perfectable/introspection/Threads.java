package org.perfectable.introspection;

import com.google.common.base.Throwables;

public final class Threads {
	
	@FunctionalInterface
	public interface ThrowingRunnable {
		void run() throws Throwable;
		
		default void runSafe() {
			try {
				run();
			}
			catch(Throwable e) { // NOPMD throwable caught intentionally
				Throwables.propagate(e);
			}
		}

		default Runnable asSafeRunnable() {
			return this::runSafe;
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
		addShutdownHook(code.asSafeRunnable());
	}
	
	private Threads() {
		// utility class
	}
	
	public static void waitForInterrupt() {
		Object monitor = new Object();
		synchronized(monitor) {
			while(true) {
				try {
					monitor.wait();
				}
				catch(InterruptedException e) {
					return;
				}
			}
		}
	}
}
