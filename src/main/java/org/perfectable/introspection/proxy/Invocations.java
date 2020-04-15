package org.perfectable.introspection.proxy;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import javax.annotation.Nullable;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

final class Invocations {
	static final class Returning implements Invocation {
		@Nullable
		private final Object result;

		Returning(@Nullable Object result) {
			this.result = result;
		}

		@Nullable
		@Override
		public Object invoke() {
			return result;
		}
	}

	static final class Throwing implements Invocation {
		private final Supplier<Throwable> thrownSupplier;

		Throwing(Supplier<Throwable> thrownSupplier) {
			this.thrownSupplier = thrownSupplier;
		}

		@CanIgnoreReturnValue
		@Nullable
		@Override
		public Object invoke() throws Throwable {
			Throwable thrown = thrownSupplier.get();
			throw thrown;
		}
	}

	static final class CallableAdapter implements Invocation {
		private final Callable<?> callable;

		CallableAdapter(Callable<?> callable) {
			this.callable = callable;
		}

		@Nullable
		@Override
		public Object invoke() throws Throwable {
			return callable.call();
		}
	}

	static final class RunnableAdapter implements Invocation {
		private final Runnable runnable;

		RunnableAdapter(Runnable runnable) {
			this.runnable = runnable;
		}

		@Nullable
		@Override
		public Object invoke() {
			runnable.run();
			return null;
		}
	}

	private Invocations() {
		// utility class
	}
}
