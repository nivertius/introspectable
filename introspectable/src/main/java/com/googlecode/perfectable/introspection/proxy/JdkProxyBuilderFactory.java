package com.googlecode.perfectable.introspection.proxy;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

public final class JdkProxyBuilderFactory implements ProxyBuilderFactory {

	private static final Set<Feature> SUPPORTED_FEATURES = EnumSet.noneOf(Feature.class);
	
	@Override
	public boolean supportsFeature(Feature requestedFeature) {
		return SUPPORTED_FEATURES.contains(requestedFeature);
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
	
	@Override
	public <I> ProxyBuilder<I> ofClass(Class<I> sourceClass) throws UnsupportedFeatureException {
		throw new UnsupportedFeatureException("JDK proxy cannot be created for classes");
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
