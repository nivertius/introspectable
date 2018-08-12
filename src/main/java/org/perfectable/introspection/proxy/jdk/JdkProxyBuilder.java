package org.perfectable.introspection.proxy.jdk;

import org.perfectable.introspection.ObjectMethods;
import org.perfectable.introspection.proxy.Invocation;
import org.perfectable.introspection.proxy.InvocationHandler;
import org.perfectable.introspection.proxy.MethodInvocation;
import org.perfectable.introspection.proxy.ProxyBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

final class JdkProxyBuilder<I> implements ProxyBuilder<I> {
	private final ClassLoader classLoader;
	private final Class<?>[] interfaces;

	private JdkProxyBuilder(ClassLoader classLoader, Class<?>... interfaces) {
		this.classLoader = classLoader;
		this.interfaces = interfaces.clone();
	}

	static <I> JdkProxyBuilder<I> of(ClassLoader classLoader, Class<?>... interfaces) {
		return new JdkProxyBuilder<I>(classLoader, interfaces);
	}

	@Override
	public I instantiate(InvocationHandler<I> handler) {
		JdkInvocationHandlerAdapter<I> adapterHandler = JdkInvocationHandlerAdapter.adapt(handler);
		try {
			@SuppressWarnings("unchecked")
			I instance = (I) Proxy.newProxyInstance(classLoader, interfaces, adapterHandler);
			return instance;
		}
		catch (IllegalArgumentException e) {
			throw new AssertionError("Proxy construction failed", e);
		}
	}

	private static final class JdkInvocationHandlerAdapter<I> implements java.lang.reflect.InvocationHandler {
		private final InvocationHandler<I> handler;

		static <I> JdkInvocationHandlerAdapter<I> adapt(InvocationHandler<I> handler) {
			return new JdkInvocationHandlerAdapter<>(handler);
		}

		private JdkInvocationHandlerAdapter(InvocationHandler<I> handler) {
			this.handler = handler;
		}

		@Nullable
		@Override
		public Object invoke(@Nullable Object proxy, Method method,
							 @Nullable Object[] args)
				throws Throwable {
			requireNonNull(method);
			if (method.equals(ObjectMethods.FINALIZE)) {
				return null; // ignore proxy finalization
			}
			@SuppressWarnings("unchecked")
			I castedProxy = (I) proxy;
			@SuppressWarnings("unchecked")
			Invocation<I> invocation = MethodInvocation.intercepted(method, castedProxy, args);
			return this.handler.handle(invocation);
		}

	}

}
