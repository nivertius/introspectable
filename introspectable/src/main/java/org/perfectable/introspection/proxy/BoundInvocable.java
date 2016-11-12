package org.perfectable.introspection.proxy;

@FunctionalInterface
public interface BoundInvocable<T> {

	// SUPPRESS NEXT IllegalThrows generic exception is actually thrown
	Object invoke(Object... arguments) throws Throwable;

	default BoundInvocation<T> prepare(Object... arguments) {
		return () -> invoke(arguments);
	}

}
