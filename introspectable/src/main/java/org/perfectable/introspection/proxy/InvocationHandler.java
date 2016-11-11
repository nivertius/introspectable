package org.perfectable.introspection.proxy;

@FunctionalInterface
public interface InvocationHandler<T> {

	Object handle(BoundInvocation<? extends T> invocation) throws Throwable;

	static <T> InvocationHandlerBuilder<T> builder(Class<T> sourceClass) {
		return StandardInvocationHandlerBuilder.start(sourceClass);
	}

}
