package org.perfectable.introspection;

import static com.google.common.base.Throwables.throwIfUnchecked;

public final class Threads {

	@FunctionalInterface
	public interface ThrowingRunnable {
		void run() throws Throwable; // SUPPRESS IllegalThrows - throwable thrown intentionally

		default void runSafe() {
			try {
				run();
			}
			catch (Throwable e) { // SUPPRESS IllegalCatch - throwable caught intentionally
				throwIfUnchecked(e);
				throw new RuntimeException(e); // SUPPRESS no better exception here
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
		synchronized (monitor) {
			while (true) {
				try {
					monitor.wait();
				}
				catch (InterruptedException e) {
					return;
				}
			}
		}
	}
}
