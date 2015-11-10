package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class Invocation<T> {

	private final Method method;
	private final Object[] args;

	public Invocation(Method method, Object[] args) {
		this.method = method;
		this.args = args;
	}

	public static Invocation<?> of(Method method, Object[] args) {
		return new Invocation<>(method, args);
	}
	
	public Object invokeOn(T receiver) throws Throwable {
		try {
			Object result = this.method.invoke(receiver, this.args);
			return result;
		}
		catch(InvocationTargetException e) {
			throw e.getCause();
		}
		catch(IllegalAccessException | IllegalArgumentException e) {
			throw new RuntimeException(e);
		}
	}
	
}
