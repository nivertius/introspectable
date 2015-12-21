package com.googlecode.perfectable.introspection.proxy;

@FunctionalInterface
public interface InvocationHandler<T> {
	
	Object handle(Invocation<T> invocation) throws Throwable;
	
}
