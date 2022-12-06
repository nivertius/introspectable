package org.perfectable.introspection.proxy;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.checkerframework.checker.nullness.qual.Nullable;

final class MethodInterceptorAdapter<I>
	implements InvocationHandler<@Nullable Object, Exception, MethodInvocation<? extends I>> {
	private final MethodInterceptor interceptor;

	MethodInterceptorAdapter(MethodInterceptor interceptor) {
		this.interceptor = interceptor;
	}

	@SuppressWarnings("IllegalCatch")
	@Override
	public @Nullable Object handle(MethodInvocation<? extends I> invocation) throws Exception {
		org.aopalliance.intercept.MethodInvocation invocationAdapter = new MethodInvocationAdapter<>(invocation);
		try {
			return interceptor.invoke(invocationAdapter);
		}
		catch (Exception | Error e) {
			throw e;
		}
		catch (Throwable e) {
			throw new AssertionError("Unknown throwable class " + e.getClass(), e);
		}
	}

	private static class MethodInvocationAdapter<I> implements org.aopalliance.intercept.MethodInvocation {
		private final MethodInvocation<? extends I> invocation;

		MethodInvocationAdapter(MethodInvocation<? extends I> invocation) {
			this.invocation = invocation;
		}

		@SuppressWarnings("override.return")
		@Override
		public @Nullable Object proceed() throws Throwable {
			return invocation.invoke();
		}

		@Override
		public Method getMethod() {
			return invocation.decompose((method, receiver, arguments) -> method);
		}

		@SuppressWarnings("override.return")
		@Override
		public @Nullable Object getThis() {
			return invocation.decompose((method, receiver, arguments) -> receiver);
		}

		@SuppressWarnings({"override.return", "return"})
		@Override
		public @Nullable Object[] getArguments() {
			return invocation.decompose((method, receiver, arguments) -> arguments);
		}

		@SuppressWarnings("override.return")
		@Override
		public @Nullable AccessibleObject getStaticPart() {
			return null;
		}
	}
}
