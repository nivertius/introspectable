package org.perfectable.introspection.proxy;

import java.lang.reflect.Method;

import org.checkerframework.checker.nullness.qual.Nullable;

final class InterceptedMethodInvocation<T> implements MethodInvocation<T> {
	private final MethodInvocation<T> wrapped;
	private final InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<T>> interceptor;

	InterceptedMethodInvocation(MethodInvocation<T> wrapped,
	                            InvocationHandler<? extends @Nullable Object, ?,
		                            ? super MethodInvocation<T>> interceptor) {
		this.wrapped = wrapped;
		this.interceptor = interceptor;
	}

	@Override
	public <R> R decompose(Decomposer<? super T, R> decomposer) {
		return wrapped.decompose(decomposer);
	}

	@Override
	public MethodInvocation<T> withMethod(Method newMethod) {
		MethodInvocation<T> updated = wrapped.withMethod(newMethod);
		return replace(updated);
	}

	@Override
	public <S extends T> MethodInvocation<S> withReceiver(S newReceiver) {
		MethodInvocation<S> updated = wrapped.withReceiver(newReceiver);
		return replace(updated);
	}

	@Override
	public MethodInvocation<T> withArguments(Object... newArguments) {
		MethodInvocation<T> updated = wrapped.withArguments(newArguments);
		return replace(updated);
	}

	@Override
	public @Nullable Object invoke() throws Exception {
		return interceptor.handle(wrapped);
	}

	private <S extends T> InterceptedMethodInvocation<S> replace(MethodInvocation<S> updated) {
		@SuppressWarnings("unchecked")
		InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<? super S>> casted =
			(InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<? super S>>) interceptor;
		return new InterceptedMethodInvocation<>(updated, casted);
	}
}
