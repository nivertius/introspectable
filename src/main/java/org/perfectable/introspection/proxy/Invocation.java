package org.perfectable.introspection.proxy;

import java.lang.reflect.Method;
import javax.annotation.Nullable;

public interface Invocation<T> {
	@Nullable
	Object invoke() throws Throwable; // SUPPRESS IllegalThrows

	@FunctionalInterface
	interface Decomposer<T, R> {
		R decompose(Method method, @Nullable T receiver, Object... arguments);
	}

	<R> R decompose(Decomposer<? super T, R> decomposer);

	default <X extends T> Invocation<X> withReceiver(X newReceiver) {
		return ReplacedReceiverInvocation.of(this, newReceiver);
	}
}
