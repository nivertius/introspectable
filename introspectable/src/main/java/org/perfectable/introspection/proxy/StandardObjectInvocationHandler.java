package org.perfectable.introspection.proxy;

import org.perfectable.introspection.Methods;

public class StandardObjectInvocationHandler implements InvocationHandler<Object> {
	public static final StandardObjectInvocationHandler INSTANCE = new StandardObjectInvocationHandler();

	@Override
	public Object handle(Invocation<Object> invocation) throws Throwable {
		MethodInvocation<?> methodInvocation = (MethodInvocation<?>) invocation;
		return methodInvocation.proceed((method, receiver, arguments) -> {
			if (Methods.OBJECT_EQUALS.equals(method)) {
				return receiver == arguments[0];
			}
			if (Methods.OBJECT_TO_STRING.equals(method)) {
				return "Proxy()";
			}
			throw new UnsupportedOperationException();
		});
	}
}
