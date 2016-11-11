package org.perfectable.introspection.proxy;

@FunctionalInterface
public interface Invocable<T> {

	Object invoke(T receiver, Object... arguments) throws Throwable;

	default BoundInvocable<T> bind(T receiver) {
		return arguments -> invoke(receiver, arguments);
	}

	default PreparedInvocable<T> prepare(Object... arguments) {
		return receiver -> invoke(receiver, arguments);
	}
}
