package com.googlecode.perfectable.introspection.proxy;

final class ForwardingHandler<T> implements InvocationHandler<T> {
	
	private T target;
	
	static <T> ForwardingHandler<T> of(T target) {
		return new ForwardingHandler<>(target);
	}
	
	private ForwardingHandler(T target) {
		this.target = target;
	}
	
	public void swap(T newTarget) {
		this.target = newTarget;
	}
	
	@Override
	public Object handle(Invocation<T> invocation) throws Throwable {
		return invocation.invokeOn(this.target);
	}
	
}
