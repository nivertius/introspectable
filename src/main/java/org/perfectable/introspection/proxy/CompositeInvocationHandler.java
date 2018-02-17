package org.perfectable.introspection.proxy;

import javax.annotation.Nullable;

final class CompositeInvocationHandler<T> implements InvocationHandler<T> {
	private final InvocationHandler<T> outer;
	private final InvocationHandler<T> inner;

	public static <X> CompositeInvocationHandler<X> of(InvocationHandler<X> outer, InvocationHandler<X> inner) {
		return new CompositeInvocationHandler<>(outer, inner);
	}

	private CompositeInvocationHandler(InvocationHandler<T> outer, InvocationHandler<T> inner) {
		this.outer = outer;
		this.inner = inner;
	}

	@Nullable
	@Override
	public Object handle(Invocation<T> invocation) throws Throwable {
		Invocation<T> nested = invocation.wrapInto(inner);
		return outer.handle(nested);
	}
}
