package com.googlecode.perfectable.introspection.proxy;

public interface BoundInvocation<T> extends Invocation {
	
	interface FunctionalInvocation<T> {
		Object invoke(T receiver, Object... arguments) throws Throwable;
	}
	
	BoundInvocation<T> withReceiver(T newReceiver);
	
	PreparedInvocable stripReceiver();
	
	BoundInvocable<T> stripArguments();
	
	Object invokeAs(FunctionalInvocation<T> function) throws Throwable;
}
