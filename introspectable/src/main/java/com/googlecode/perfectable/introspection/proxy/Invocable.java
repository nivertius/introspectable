package com.googlecode.perfectable.introspection.proxy;

public interface Invocable {
	
	<T> BoundInvocable<T> bind(T receiver);
	
	PreparedInvocable prepare(Object... arguments);
	
	Class<?> expectedResultType();
	
}
