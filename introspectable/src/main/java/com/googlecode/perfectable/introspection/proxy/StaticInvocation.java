package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class StaticInvocation implements Invocation {
	private final Method method;
	private final Object[] arguments;
	
	public static StaticInvocation of(Method method, Object... arguments) {
		return new StaticInvocation(method, arguments);
	}
	
	private StaticInvocation(Method method, Object... arguments) {
		this.method = method;
		this.arguments = arguments;
	}
	
	@Override
	public void decompose(Decomposer decomposer) {
		DecompositionHelper.start(decomposer)
				.method(this.method)
				.arguments(this.arguments);
	}
	
	@Override
	public Object invoke() throws Throwable {
		try {
			Object result = this.method.invoke(null, this.arguments);
			return result;
		}
		catch(InvocationTargetException e) {
			throw e.getCause();
		}
	}
}
