package com.googlecode.perfectable.introspection.proxy;

import static com.google.common.base.Preconditions.checkState;

public class StandardObjectInvocationHandler implements InvocationHandler<Object> {
	public static final StandardObjectInvocationHandler INSTANCE = new StandardObjectInvocationHandler();
	
	@SuppressWarnings("boxing")
	@Override
	public Object handle(BoundInvocation<?> invocation) throws Throwable {
		Invocable invocable = invocation.stripArguments().stripReceiver();
		MethodInvocable methodInvocable = (MethodInvocable) invocable;
		checkState(methodInvocable.isDeclaredBy(Object.class));
		if(MethodInvocable.OBJECT_EQUALS.equals(methodInvocable)) {
			return invocation.invokeAs((receiver, arguments) -> receiver == arguments[0]);
		}
		if(MethodInvocable.OBJECT_TO_STRING.equals(methodInvocable)) {
			return invocation.invokeAs((receiver, arguments) -> "Proxy(" + this + ")");
		}
		throw new UnsupportedOperationException("Unimplemented: " + invocable);
	}
}
