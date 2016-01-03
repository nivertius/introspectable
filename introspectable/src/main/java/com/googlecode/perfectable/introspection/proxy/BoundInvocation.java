package com.googlecode.perfectable.introspection.proxy;

public interface BoundInvocation<T> extends Invocation {
	
	BoundInvocation<T> withReceiver(T newReceiver);
	
	PreparedInvocable stripReceiver();
	
	BoundInvocable<T> stripArguments();
}
