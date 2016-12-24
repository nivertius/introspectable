package org.perfectable.introspection.proxy;

import java.lang.reflect.Method;

import static org.perfectable.introspection.Introspections.introspect;

public class StandardObjectInvocationHandler implements InvocationHandler<Object> {
	public static final StandardObjectInvocationHandler INSTANCE = new StandardObjectInvocationHandler();

	private static final Method OBJECT_EQUALS =
			introspect(Object.class).methods().named("equals").parameters(Object.class).single();
	private static final Method OBJECT_TO_STRING =
			introspect(Object.class).methods().named("toString").parameters().single();

	@Override
	public Object handle(Invocation<Object> invocation) throws Throwable {
		return invocation.proceed((method, receiver, arguments) -> {
			if (OBJECT_EQUALS.equals(method)) {
				return receiver == arguments[0];
			}
			if (OBJECT_TO_STRING.equals(method)) {
				return "Proxy()";
			}
			throw new UnsupportedOperationException();
		});
	}
}
