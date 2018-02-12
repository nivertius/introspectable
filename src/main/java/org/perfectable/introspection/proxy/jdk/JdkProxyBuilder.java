package org.perfectable.introspection.proxy.jdk;

import org.perfectable.introspection.ObjectMethods;
import org.perfectable.introspection.proxy.Invocation;
import org.perfectable.introspection.proxy.InvocationHandler;
import org.perfectable.introspection.proxy.MethodInvocation;
import org.perfectable.introspection.proxy.ProxyBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

final class JdkProxyBuilder<I> implements ProxyBuilder<I> {

	private final Class<I> proxyClass;

	static <X> JdkProxyBuilder<X> ofProxyClass(Class<X> proxyClass) {
		checkArgument(Proxy.isProxyClass(proxyClass));
		return new JdkProxyBuilder<>(proxyClass);
	}

	private JdkProxyBuilder(Class<I> proxyClass) {
		this.proxyClass = proxyClass;
	}

	@Override
	public I instantiate(InvocationHandler<I> handler) {
		Constructor<I> constructor;
		try {
			constructor = this.proxyClass.getConstructor(java.lang.reflect.InvocationHandler.class);
		}
		catch (NoSuchMethodException | IllegalArgumentException e) {
			throw new AssertionError("Proxy class must have constructor with InvocationHandler", e);
		}
		JdkInvocationHandlerAdapter<I> adapterHandler = JdkInvocationHandlerAdapter.adapt(handler);
		try {
			return constructor.newInstance(adapterHandler);
		}
		catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new AssertionError("Proxy class constructor must be possible to call with invocation handler", e);
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
							 @Nullable Object[] args) // SUPPRESS declaration uses array not varargs
				throws Throwable { // SUPPRESS throwable is actually thrown here
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
