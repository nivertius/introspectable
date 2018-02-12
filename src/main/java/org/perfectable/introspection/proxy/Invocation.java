package org.perfectable.introspection.proxy;

import java.lang.reflect.Method;
import javax.annotation.Nullable;

public interface Invocation<T> {
	@FunctionalInterface
	interface Invoker<T> {
		// SUPPRESS NEXT 2 IllegalThrows
		@Nullable
		Object process(Method method, @Nullable T receiver, Object... arguments) throws Throwable;
	}

	@Nullable
	default Object invoke() throws Throwable { // SUPPRESS IllegalThrows
		return proceed(MethodInvoker.INSTANCE);
	}

	@Nullable
	Object proceed(Invoker<? super T> invoker) throws Throwable; // SUPPRESS IllegalThrows

	@FunctionalInterface
	interface Decomposer<T, R> {
		R decompose(Method method, @Nullable T receiver, Object... arguments);
	}

	<R> R decompose(Decomposer<? super T, R> decomposer);

	default <X extends T> Invocation<X> withReceiver(X newReceiver) {
		return ReplacedReceiverInvocation.of(this, newReceiver);
	}
}
