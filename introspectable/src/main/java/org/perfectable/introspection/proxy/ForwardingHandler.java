package org.perfectable.introspection.proxy;


public final class ForwardingHandler<T> implements InvocationHandler<T> {

	private T target;

	public static <T> ForwardingHandler<T> of(T target) {
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
		@SuppressWarnings("unchecked")
		MethodInvocation<T> methodInvocation = (MethodInvocation<T>) invocation;
		return methodInvocation.replaceReceiver(this.target).invoke();
	}

}
