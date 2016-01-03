package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class MethodStaticInvocation implements StaticInvocation {
	private final Method method;
	private final Object[] arguments;
	
	public static MethodStaticInvocation of(Method method, Object... arguments) {
		return new MethodStaticInvocation(method, arguments);
	}
	
	private MethodStaticInvocation(Method method, Object... arguments) {
		this.method = method;
		this.arguments = arguments;
	}
	
	public void decompose(Invocation.Decomposer decomposer) {
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
	
	@Override
	public Invocable stripArguments() {
		return MethodInvocable.of(this.method);
	}
}
