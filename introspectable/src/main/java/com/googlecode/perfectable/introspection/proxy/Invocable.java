package com.googlecode.perfectable.introspection.proxy;

public interface Invocable {
	
	<T> BoundInvocable<T> bind(T receiver);
	
	void decompose(Invocation.Decomposer decomposer);
	
	PreparedInvocable prepare(Object... arguments);
	
	Class<?> expectedResultType();
	
}
