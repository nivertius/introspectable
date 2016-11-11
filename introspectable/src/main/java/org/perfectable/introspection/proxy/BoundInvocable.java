package org.perfectable.introspection.proxy;

@FunctionalInterface
public interface BoundInvocable<T> {

	Object invoke(Object... arguments) throws Throwable;

	default BoundInvocation<T> prepare(Object... arguments) {
		return () -> invoke(arguments);
	}

}
