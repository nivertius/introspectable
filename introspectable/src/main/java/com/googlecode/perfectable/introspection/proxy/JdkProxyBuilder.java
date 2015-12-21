package com.googlecode.perfectable.introspection.proxy;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.collect.ObjectArrays;
import com.googlecode.perfectable.introspection.Introspection;

public final class JdkProxyBuilder<I> implements ProxyBuilder<I> {
	
	public static <I> JdkProxyBuilder<I> ofInterfacesOf(Class<? extends I> implementingClass) {
		Class<?>[] interfaces = Introspection.of(implementingClass).interfaces().stream()
				.toArray(Class[]::new);
		// MARK this is safe almost always?
		@SuppressWarnings("unchecked")
		final JdkProxyBuilder<I> builder = (JdkProxyBuilder<I>) ofInterfaces(interfaces);
		return builder;
	}
	
	public static <I> JdkProxyBuilder<I> sameAs(I sourceInstance) {
		@SuppressWarnings("unchecked")
		Class<? extends I> implementingClass = (Class<? extends I>) sourceInstance.getClass();
		return ofInterfacesOf(implementingClass);
	}
	
	public static <X> JdkProxyBuilder<X> ofInterfaces(Class<X> mainInterface, Class<?>... otherInterfaces) {
		Class<?>[] usedInterfaces = ObjectArrays.concat(mainInterface, otherInterfaces);
		@SuppressWarnings("unchecked")
		JdkProxyBuilder<X> casted = (JdkProxyBuilder<X>) ofInterfaces(usedInterfaces);
		return casted;
	}
	
	public static JdkProxyBuilder<?> ofInterfaces(Class<?>... interfaces) {
		checkArgument(interfaces.length > 0);
		Stream.of(interfaces).forEach(JdkProxyBuilder::checkProxyableInterface);
		ClassLoader classLoader = interfaces[0].getClassLoader();
		checkClassloader(classLoader, interfaces);
		Class<?> proxyClass = Proxy.getProxyClass(classLoader, interfaces);
		return ofProxyClass(proxyClass);
	}
	
	public static <X> JdkProxyBuilder<X> ofProxyClass(Class<X> proxyClass) {
		checkArgument(Proxy.isProxyClass(proxyClass));
		return new JdkProxyBuilder<>(proxyClass);
	}
	
	private final Class<I> proxyClass;
	
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
			throw new RuntimeException(e);
		}
		JdkInvocationHandlerAdapter adapterHandler = JdkInvocationHandlerAdapter.adapt(handler);
		try {
			return constructor.newInstance(adapterHandler);
		}
		catch(InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}
	
	private static void checkProxyableInterface(Class<?> testedInterface) {
		checkArgument(testedInterface.isInterface());
		checkArgument(!testedInterface.isPrimitive());
	}
	
	private static void checkClassloader(final ClassLoader classLoader, Class<?>... otherInterfaces) {
		Stream.of(otherInterfaces)
				.forEach(i -> checkArgument(classLoader.equals(i.getClassLoader())));
	}
	
	private static class JdkInvocationHandlerAdapter implements java.lang.reflect.InvocationHandler {
		
		private final InvocationHandler<?> handler;
		
		public JdkInvocationHandlerAdapter(InvocationHandler<?> handler) {
			this.handler = handler;
		}
		
		public static JdkInvocationHandlerAdapter adapt(InvocationHandler<?> handler) {
			return new JdkInvocationHandlerAdapter(handler);
		}
		
		@Override
		public Object invoke(@Nullable Object proxy, @Nullable Method method, @Nullable Object[] args) throws Throwable {
			checkNotNull(method);
			checkNotNull(args);
			@SuppressWarnings("unchecked")
			Invocation<Object> invocation = (Invocation<Object>) Invocation.of(method, args);
			@SuppressWarnings("unchecked")
			InvocationHandler<Object> castedHandler = (InvocationHandler<Object>) this.handler;
			return castedHandler.handle(invocation);
		}
		
	}
	
}
