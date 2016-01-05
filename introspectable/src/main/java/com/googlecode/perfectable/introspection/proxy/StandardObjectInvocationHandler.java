package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.Method;

public class StandardObjectInvocationHandler implements InvocationHandler<Object> {
	public static final StandardObjectInvocationHandler INSTANCE = new StandardObjectInvocationHandler();
	
	@Override
	public Object handle(BoundInvocation<?> invocation) throws Throwable {
		MethodBoundInvocation<?> methodInvocation = (MethodBoundInvocation<?>) invocation;
		MethodBoundInvocationMappingDecomposer<Object> mappingDecomposer =
				MethodBoundInvocationMappingDecomposer.identity().withMethodTransformer(
						StandardObjectInvocationHandler::findInvocable);
		BoundInvocation<?> replacedInvocable = methodInvocation.decompose(mappingDecomposer);
		return replacedInvocable.invoke();
	}
	
	@SuppressWarnings("boxing")
	private static Invocable<Object> findInvocable(Method objectMethod) {
		if(MethodInvocable.OBJECT_EQUALS.equals(MethodInvocable.of(objectMethod))) {
			return (receiver, arguments) -> receiver == arguments[0];
		}
		if(MethodInvocable.OBJECT_TO_STRING.equals(MethodInvocable.of(objectMethod))) {
			return (receiver, arguments) -> "Proxy()";
		}
		throw new UnsupportedOperationException();
	}
}
