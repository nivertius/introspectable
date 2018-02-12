package org.perfectable.introspection.proxy;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

final class MethodInvoker implements Invocation.Invoker<Object> {
	public static final MethodInvoker INSTANCE = new MethodInvoker();

	private MethodInvoker() {
		// singleton
	}

	@Nullable
	@Override
	public Object process(Method method, @Nullable Object receiver, Object... arguments) throws Throwable {
		Object[] actualArguments = composeVariableArguments(method, arguments);
		try {
			return method.invoke(receiver, actualArguments);
		}
		catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	private static Object[] composeVariableArguments(Method method, Object[] provided) { // SUPPRESS UseVarargs
		if (!method.isVarArgs()) {
			return provided;
		}
		Class<?>[] formals = method.getParameterTypes();
		Class<?> variableFormal = formals[formals.length - 1].getComponentType();
		int variableLength = provided.length - (formals.length - 1);
		int resultSize = formals.length;
		Object[] result = new Object[resultSize];
		System.arraycopy(provided, 0, result, 0, formals.length - 1);
		Object variableActual = Array.newInstance(variableFormal, variableLength);
		for (int i = 0; i < variableLength; i++) {
			Object value = provided[(formals.length - 1) + i];
			Array.set(variableActual, i, value);
		}
		result[formals.length - 1] = variableActual;
		return result;
	}
}
