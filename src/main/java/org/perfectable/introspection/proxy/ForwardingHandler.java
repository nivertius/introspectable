package org.perfectable.introspection.proxy;


import javax.annotation.Nullable;

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

	@Nullable
	@Override
	public Object handle(Invocation<T> invocation) throws Throwable {
		Invocation<T> replacedReceiver =
				invocation.decompose((method, receiver, arguments) -> Invocation.of(method, this.target, arguments));
		return replacedReceiver.invoke();
	}

}
