package org.perfectable.introspection.proxy;

@FunctionalInterface
public interface InvocationHandler<T> {

	// SUPPRESS NEXT IllegalThrows generic exception is actually thrown
	Object handle(BoundInvocation<? extends T> invocation) throws Throwable;

	static <T> InvocationHandlerBuilder<T> builder(Class<T> sourceClass) {
		return StandardInvocationHandlerBuilder.start(sourceClass);
	}

}
