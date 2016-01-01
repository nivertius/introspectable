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
	public Object handle(BoundInvocation<T> invocation) throws Throwable {
		return invocation.withReceiver(this.target).invoke();
	}
	
}
