package org.perfectable.introspection.proxy;

import javax.annotation.Nullable;

@FunctionalInterface
public interface InvocationHandler<T> {

	// SUPPRESS NEXT 2 IllegalThrows generic exception is actually thrown
	@Nullable
	Object handle(Invocation<T> invocation) throws Throwable;

	default InvocationHandler<T> andThen(InvocationHandler<T> other) {
		return CompositeInvocationHandler.of(this, other);
	}
}
