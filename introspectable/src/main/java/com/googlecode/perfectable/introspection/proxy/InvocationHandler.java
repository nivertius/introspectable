package com.googlecode.perfectable.introspection.proxy;

@FunctionalInterface
public interface InvocationHandler<T> {
	
	Object handle(BoundInvocation<T> invocation) throws Throwable;
	
}
