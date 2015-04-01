package com.googlecode.perfectable.introspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

import com.google.common.collect.Iterables;
import com.google.common.collect.ObjectArrays;

public final class ProxyBuilder<I> {

	public static <X> ProxyBuilder<X> ofInterfaces(Class<X> mainInterface, Class<?>... otherInterfaces) {
		// MARK check if all interfaces are from same classloader
		// MARK check if all arguments are interfaces
		final ClassLoader classLoader = mainInterface.getClassLoader();
		Class<? extends Object>[] usedInterfaces = ObjectArrays.concat(mainInterface, otherInterfaces);
		@SuppressWarnings("unchecked")
		Class<X> proxyClass = (Class<X>) Proxy.getProxyClass(classLoader, usedInterfaces);
		return ofProxyClass(proxyClass);
	}

	public static <X> ProxyBuilder<X> ofProxyClass(Class<X> proxyClass) {
	    // MARK check if class is actually proxy class
		return new ProxyBuilder<>(proxyClass);
	}
	
	private final Class<I> proxyClass;
	
	public I instantiate(InvocationHandler handler) {
		try {
			Constructor<I> constructor = this.proxyClass.getConstructor(InvocationHandler.class);
			return constructor.newInstance(handler);
		}
		catch(NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	private ProxyBuilder(Class<I> proxyClass) {
		this.proxyClass = proxyClass;
	}
}
