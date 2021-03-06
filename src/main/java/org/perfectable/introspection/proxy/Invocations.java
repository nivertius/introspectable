package org.perfectable.introspection.proxy;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;

final class Invocations {
	static final class Returning implements Invocation {
		private final @Nullable Object result;

		Returning(@Nullable Object result) {
			this.result = result;
		}

		@Override
		public @Nullable Object invoke() {
			return result;
		}
	}

	static final class Throwing implements Invocation {
		private final Supplier<Throwable> thrownSupplier;

		Throwing(Supplier<Throwable> thrownSupplier) {
			this.thrownSupplier = thrownSupplier;
		}

		@CanIgnoreReturnValue
		@Override
		public @Nullable Object invoke() throws Throwable {
			throw thrownSupplier.get();
		}
	}

	static final class CallableAdapter implements Invocation {
		private final Callable<?> callable;

		CallableAdapter(Callable<?> callable) {
			this.callable = callable;
		}

		@Override
		public @Nullable Object invoke() throws Throwable {
			return callable.call();
		}
	}

	static final class RunnableAdapter implements Invocation {
		private final Runnable runnable;

		RunnableAdapter(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public @Nullable Object invoke() {
			runnable.run();
			return null;
		}
	}

	private Invocations() {
		// utility class
	}
}
