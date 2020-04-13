package org.perfectable.introspection.proxy;

import java.util.concurrent.Callable;
import java.util.function.Supplier;
import javax.annotation.Nullable;

@FunctionalInterface
public interface Invocation {
	@Nullable
	Object invoke() throws Throwable; // SUPPRESS IllegalThrows

	static Invocation fromRunnable(Runnable runnable) {
		return new Invocations.RunnableAdapter(runnable);
	}

	static Invocation fromCallable(Callable<?> callable) {
		return new Invocations.CallableAdapter(callable);
	}

	static Invocation returning(@Nullable Object result) {
		return new Invocations.Returning(result);
	}

	static Invocation throwing(Supplier<Throwable> thrownSupplier) {
		return new Invocations.Throwing(thrownSupplier);
	}

}
