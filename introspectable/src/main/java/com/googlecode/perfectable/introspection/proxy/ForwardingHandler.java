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
	public Object handle(BoundInvocation<? extends T> invocation) throws Throwable {
		return ((BoundInvocation<T>) invocation).withReceiver(this.target).invoke(); // MARK unchecked
	}
	
}
