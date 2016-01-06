package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.Method;

import com.googlecode.perfectable.introspection.Methods;

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
		if(Methods.OBJECT_EQUALS.equals(objectMethod)) {
			return (receiver, arguments) -> receiver == arguments[0];
		}
		if(Methods.OBJECT_TO_STRING.equals(objectMethod)) {
			return (receiver, arguments) -> "Proxy()";
		}
		throw new UnsupportedOperationException();
	}
}
