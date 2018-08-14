package org.perfectable.introspection.proxy.javassist;

import org.perfectable.introspection.ObjectMethods;
import org.perfectable.introspection.proxy.InvocationHandler;
import org.perfectable.introspection.proxy.MethodInvocation;
import org.perfectable.introspection.proxy.ProxyBuilder;

import java.lang.reflect.Method;
import javax.annotation.Nullable;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import org.objenesis.instantiator.ObjectInstantiator;

import static java.util.Objects.requireNonNull;

final class JavassistProxyBuilder<I> implements ProxyBuilder<I> {

	private final ObjectInstantiator<I> instantiator;

	public static <I> JavassistProxyBuilder<I> create(ObjectInstantiator<I> instantiator) {
		return new JavassistProxyBuilder<>(instantiator);
	}

	private JavassistProxyBuilder(ObjectInstantiator<I> instantiator) {
		this.instantiator = instantiator;
	}

	@Override
	public I instantiate(InvocationHandler<? super MethodInvocation<I>> handler) {
		MethodHandler handlerAdapter = JavassistInvocationHandlerAdapter.adapt(handler);
		I proxy = this.instantiator.newInstance();
		((Proxy) proxy).setHandler(handlerAdapter);
		return proxy;
	}

	private static final class JavassistInvocationHandlerAdapter<T> implements MethodHandler {
		private final InvocationHandler<? super MethodInvocation<T>> handler;

		static <X> JavassistInvocationHandlerAdapter<X> adapt(InvocationHandler<? super MethodInvocation<X>> handler) {
			return new JavassistInvocationHandlerAdapter<>(handler);
		}

		private JavassistInvocationHandlerAdapter(InvocationHandler<? super MethodInvocation<T>> handler) {
			this.handler = handler;
		}

		@Nullable
		@Override
		public Object invoke(@Nullable Object self, Method thisMethod, Method proceed,
							 @Nullable Object[] args)
				throws Throwable {
			requireNonNull(thisMethod);
			if (thisMethod.equals(ObjectMethods.FINALIZE)) {
				return null; // ignore proxy finalization
			}
			@SuppressWarnings("unchecked")
			T castedSelf = (T) self;
			@SuppressWarnings("unchecked")
			MethodInvocation<T> invocation = MethodInvocation.intercepted(thisMethod, castedSelf, args);
			return this.handler.handle(invocation);
		}
	}
}
