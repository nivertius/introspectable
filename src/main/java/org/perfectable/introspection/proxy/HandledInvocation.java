package org.perfectable.introspection.proxy;

import javax.annotation.Nullable;

final class HandledInvocation<T> implements Invocation<T> {
	private final Invocation<T> invocation;
	private final InvocationHandler<T> handler;

	public static <X> HandledInvocation<X> of(Invocation<X> invocation, InvocationHandler<X> handler) {
		return new HandledInvocation<>(invocation, handler);
	}

	private HandledInvocation(Invocation<T> invocation, InvocationHandler<T> handler) {
		this.handler = handler;
		this.invocation = invocation;
	}

	@Nullable
	@Override
	public Object invoke() throws Throwable {
		return handler.handle(invocation);
	}

	@Override
	public <R> R decompose(Decomposer<? super T, R> decomposer) {
		return invocation.decompose(decomposer);
	}
}
