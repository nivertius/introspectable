package org.perfectable.introspection.proxy;

import javax.annotation.Nullable;

@FunctionalInterface
public interface InvocationHandler<I extends Invocation> {

	// SUPPRESS NEXT 2 IllegalThrows generic exception is actually thrown
	@Nullable
	Object handle(I invocation) throws Throwable;
}
