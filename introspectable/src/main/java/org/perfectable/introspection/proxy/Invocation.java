package org.perfectable.introspection.proxy;

public interface Invocation<T> {

	// SUPPRESS NEXT IllegalThrows
	Object invoke() throws Throwable;
}
