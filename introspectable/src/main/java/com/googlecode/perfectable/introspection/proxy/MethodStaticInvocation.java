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
	
	public interface Decomposer {
		void method(Method method);
		
		<X> void argument(int index, Class<? super X> formal, X actual);
	}
	
	public void decompose(Decomposer decomposer) {
		decomposer.method(this.method);
		DecompositionHelper.decomposeArguments(this.method, this.arguments, decomposer::argument);
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
