package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.Method;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;

import org.objenesis.instantiator.ObjectInstantiator;

final class JavassistProxyBuilder<I> implements ProxyBuilder<I> {
	
	private final ObjectInstantiator<I> instantiator;
	
	public static <I> JavassistProxyBuilder<I> create(ObjectInstantiator<I> instantiator) {
		return new JavassistProxyBuilder<>(instantiator);
	}
	
	private JavassistProxyBuilder(ObjectInstantiator<I> instantiator) {
		this.instantiator = instantiator;
	}
	
	@Override
	public I instantiate(InvocationHandler<I> handler) {
		MethodHandler handlerAdapter = JavassistInvocationHandlerAdapter.adapt(handler);
		I proxy = this.instantiator.newInstance();
		((Proxy) proxy).setHandler(handlerAdapter);
		return proxy;
	}
	
	private static class JavassistInvocationHandlerAdapter<I> implements MethodHandler {
		
		private final InvocationHandler<I> handler;
		
		public JavassistInvocationHandlerAdapter(InvocationHandler<I> handler) {
			this.handler = handler;
		}
		
		public static <I> JavassistProxyBuilder.JavassistInvocationHandlerAdapter<I> adapt(InvocationHandler<I> handler) {
			return new JavassistProxyBuilder.JavassistInvocationHandlerAdapter<>(handler);
		}
		
		@Override
		public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable { // NOPMD
			// declaration uses array instead of varargs
			@SuppressWarnings("unchecked")
			BoundInvocation<I> invocation = (BoundInvocation<I>) MethodInvocable.of(thisMethod).prepare(args).bind(self);
			return this.handler.handle(invocation);
		}
	}
}
