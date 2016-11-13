package org.perfectable.introspection.proxy;

import org.perfectable.introspection.Methods;

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

	private static final class JavassistInvocationHandlerAdapter<I> implements MethodHandler {

		private final InvocationHandler<I> handler;

		static <I> JavassistInvocationHandlerAdapter<I> adapt(InvocationHandler<I> handler) {
			return new JavassistInvocationHandlerAdapter<>(handler);
		}

		private JavassistInvocationHandlerAdapter(InvocationHandler<I> handler) {
			this.handler = handler;
		}

		@Override
		public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) // SUPPRESS
				throws Throwable { // SUPPRESS throwble is actually thrown here
			if (thisMethod.equals(Methods.OBJECT_FINALIZE)) {
				return null; // ignore proxy finalization
			}
			@SuppressWarnings("unchecked")
			MethodInvocable<I> invocable = (MethodInvocable<I>) MethodInvocable.of(thisMethod);
			@SuppressWarnings("unchecked")
			I castedSelf = (I) self;
			BoundInvocation<I> invocation = invocable.prepare(args).bind(castedSelf);
			return this.handler.handle(invocation);
		}
	}
}
