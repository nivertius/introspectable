package org.perfectable.introspection.proxy;

@FunctionalInterface
public interface PreparedInvocable<T> {

	// SUPPRESS NEXT IllegalThrows generic exception is actually thrown
	Object invoke(T receiver) throws Throwable;

	default BoundInvocation<T> bind(T receiver) {
		return () -> invoke(receiver);
	}

	default StaticInvocation asStatic() {
		return () -> invoke(null);
	}

}
