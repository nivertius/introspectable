package org.perfectable.introspection.proxy;


@FunctionalInterface
public interface Invocation {
	// SUPPRESS NEXT IllegalThrows generic exception is actually thrown
	Object invoke() throws Throwable;
}
