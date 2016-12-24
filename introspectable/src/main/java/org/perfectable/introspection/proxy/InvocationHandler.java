package org.perfectable.introspection.proxy;

@FunctionalInterface
public interface InvocationHandler<T> {

	// SUPPRESS NEXT IllegalThrows generic exception is actually thrown
	Object handle(Invocation<T> invocation) throws Throwable;
}
