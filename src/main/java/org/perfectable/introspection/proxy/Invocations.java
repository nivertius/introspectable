package org.perfectable.introspection.proxy;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

final class Invocations {
	static final class Returning<T> implements Invocation<T, RuntimeException> {
		private final T result;

		Returning(T result) {
			this.result = result;
		}

		@Override
		public T invoke() {
			return result;
		}
	}

	static final class Throwing<X extends Exception> implements Invocation<Void, X> {
		private final Supplier<X> thrownSupplier;

		Throwing(Supplier<X> thrownSupplier) {
			this.thrownSupplier = thrownSupplier;
		}

		@Override
		public Void invoke() throws X {
			throw thrownSupplier.get();
		}
	}

	static final class CallableAdapter<R> implements Invocation<R, Exception> {
		private final Callable<R> callable;

		CallableAdapter(Callable<R> callable) {
			this.callable = callable;
		}

		@Override
		public R invoke() throws Exception {
			return callable.call();
		}
	}

	static final class RunnableAdapter implements Invocation<Void, RuntimeException> {
		private final Runnable runnable;

		RunnableAdapter(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public Void invoke() {
			runnable.run();
			return null;
		}
	}

	static final class Casting<R, S, X extends Exception> implements Invocation<S, X> {
		private final Invocation<R, X> parent;
		private final Class<S> targetCast;

		Casting(Invocation<R, X> parent, Class<S> targetCast) {
			this.parent = parent;
			this.targetCast = targetCast;
		}

		@Override
		public S invoke() throws X {
			@SuppressWarnings("assignment.type.incompatible")
			R result = parent.invoke();
			return targetCast.cast(result);
		}
	}

	private Invocations() {
		// utility class
	}
}
