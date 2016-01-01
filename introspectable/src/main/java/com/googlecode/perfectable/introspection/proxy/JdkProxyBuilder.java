package com.googlecode.perfectable.introspection.proxy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.annotation.Nullable;

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
		catch(NoSuchMethodException | IllegalArgumentException e) {
			throw new AssertionError("Proxy class must have constructor with InvocationHandler", e);
		}
		JdkInvocationHandlerAdapter<I> adapterHandler = JdkInvocationHandlerAdapter.adapt(handler);
		try {
			return constructor.newInstance(adapterHandler);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new AssertionError("Proxy class constructor must be possible to call with invocation handler", e);
		}
	}
	
	private static class JdkInvocationHandlerAdapter<I> implements java.lang.reflect.InvocationHandler {
		
		private final InvocationHandler<I> handler;
		
		public JdkInvocationHandlerAdapter(InvocationHandler<I> handler) {
			this.handler = handler;
		}
		
		public static <I> JdkInvocationHandlerAdapter<I> adapt(InvocationHandler<I> handler) {
			return new JdkInvocationHandlerAdapter<>(handler);
		}
		
		@Override
		public Object invoke(@Nullable Object proxy, @Nullable Method method, @Nullable Object[] args) throws Throwable { // NOPMD
			// declaration uses array instead of varargs
			checkNotNull(method);
			@SuppressWarnings("unchecked")
			BoundInvocation<I> invocation = (BoundInvocation<I>) Invocable.of(method).prepare(args).bind(proxy);
			return this.handler.handle(invocation);
		}
		
	}
	
}
