package org.perfectable.introspection.proxy.jdk;

import org.perfectable.introspection.proxy.ProxyBuilder;
import org.perfectable.introspection.proxy.ProxyBuilderFactory;

import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;

public final class JdkProxyBuilderFactory implements ProxyBuilderFactory {

	private static final Set<Feature> SUPPORTED_FEATURES = EnumSet.noneOf(Feature.class);

	@Override
	public boolean supportsFeature(Feature requestedFeature) {
		return SUPPORTED_FEATURES.contains(requestedFeature);
	}

	@Override
	public ProxyBuilder<?> ofInterfaces(Class<?>... interfaces) {
		checkArgument(interfaces.length > 0);
		Stream.of(interfaces).forEach(ProxyBuilderFactory::checkProxyableInterface);
		ClassLoader classLoader =
				interfaces[0].getClassLoader(); // SUPPRESS we actually want first interface classloader
		// here
		ProxyBuilderFactory.checkClassloader(classLoader, interfaces);
		Class<?> proxyClass = Proxy.getProxyClass(classLoader, interfaces);
		return JdkProxyBuilder.ofProxyClass(proxyClass);
	}

	@Override
	public <I> ProxyBuilder<I> ofClass(Class<I> sourceClass, Class<?>... additionalInterfaces)
			throws UnsupportedFeatureException {
		throw new UnsupportedFeatureException("JDK proxy cannot be created for classes");
	}

	// constructor must be public, this class is instantiated from ServiceLoader
}
