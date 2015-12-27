package com.googlecode.perfectable.introspection.proxy;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Proxy;
import java.util.stream.Stream;

import com.google.common.collect.ObjectArrays;
import com.googlecode.perfectable.introspection.Introspection;

public final class JdkProxyBuilderFactory implements ProxyBuilderFactory {

	@Override
	public boolean supportsFeature(Feature requestedFeature) {
		switch(requestedFeature) {
			default:
				throw new AssertionError("Unknown feature");
		}
	}

	@Override
	public <I> JdkProxyBuilder<I> sameAs(I sourceInstance) {
		@SuppressWarnings("unchecked")
		Class<? extends I> implementingClass = (Class<? extends I>) sourceInstance.getClass();
		return ofInterfacesOf(implementingClass);
	}

	@Override
	public JdkProxyBuilder<?> ofInterfaces(Class<?>... interfaces) {
		checkArgument(interfaces.length > 0);
		Stream.of(interfaces).forEach(JdkProxyBuilderFactory::checkProxyableInterface);
		ClassLoader classLoader = interfaces[0].getClassLoader(); // NOPMD we actually want first interface classloader
																															// here
		checkClassloader(classLoader, interfaces);
		Class<?> proxyClass = Proxy.getProxyClass(classLoader, interfaces);
		return JdkProxyBuilder.ofProxyClass(proxyClass);
	}
	
	private static void checkClassloader(final ClassLoader classLoader, Class<?>... otherInterfaces) {
		Stream.of(otherInterfaces)
				.forEach(i -> checkArgument(classLoader.equals(i.getClassLoader())));
	}
	
	private static void checkProxyableInterface(Class<?> testedInterface) {
		checkArgument(testedInterface.isInterface());
		checkArgument(!testedInterface.isPrimitive());
	}
	
}
