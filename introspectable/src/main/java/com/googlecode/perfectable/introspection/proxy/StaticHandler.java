package com.googlecode.perfectable.introspection.proxy;

final class StaticHandler<T> implements InvocationHandler<T> {
	
	private static final StaticHandler<?> INSTANCE = new StaticHandler<>();
	
	@SuppressWarnings("unchecked")
	static <T> StaticHandler<T> of() {
		return (StaticHandler<T>) INSTANCE;
	}
	
	private StaticHandler() {
		// singleton
	}
	
	@Override
	public Object handle(Invocation<T> invocation) throws Throwable {
		return invocation.invokeAsStatic();
	}
	
}
