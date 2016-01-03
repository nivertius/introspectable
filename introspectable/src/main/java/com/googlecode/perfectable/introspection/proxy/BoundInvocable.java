package com.googlecode.perfectable.introspection.proxy;

public interface BoundInvocable<T> {
	
	BoundInvocation<T> prepare(Object... arguments);
	
	Invocable stripReceiver();
	
}
