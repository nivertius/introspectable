package com.googlecode.perfectable.introspection.proxy;

public interface PreparedInvocable {
	
	<T> BoundInvocation<T> bind(T receiver);
	
	StaticInvocation asStatic();
	
	Invocable stripArguments();

}
