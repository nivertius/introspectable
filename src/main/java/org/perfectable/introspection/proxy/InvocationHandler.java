package org.perfectable.introspection.proxy;

import javax.annotation.Nullable;

@FunctionalInterface
public interface InvocationHandler<T> {

	// SUPPRESS NEXT 2 IllegalThrows generic exception is actually thrown
	@Nullable
	Object handle(Invocation<T> invocation) throws Throwable;
}
