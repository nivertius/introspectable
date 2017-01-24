package org.perfectable.introspection.proxy.jdk;

import org.perfectable.introspection.proxy.AbstractProxyBuilderFactory;
import org.perfectable.introspection.proxy.ProxyBuilder;

import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.Set;

public final class JdkProxyBuilderFactory extends AbstractProxyBuilderFactory {

	private static final Set<Feature> SUPPORTED_FEATURES = EnumSet.noneOf(Feature.class);

	public JdkProxyBuilderFactory() {
		// constructor must be public, this class is instantiated from ServiceLoader
		super(SUPPORTED_FEATURES);
	}

	@Override
	public ProxyBuilder<?> ofInterfacesSafe(ClassLoader classLoader, Class<?>... interfaces) {
		Class<?> proxyClass = Proxy.getProxyClass(classLoader, interfaces);
		return JdkProxyBuilder.ofProxyClass(proxyClass);
	}

	@Override
	protected <I> ProxyBuilder<I> ofClassSafe(ClassLoader classLoader,
											  Class<I> sourceClass, Class<?>... additionalInterfaces)
			throws UnsupportedFeatureException {
		throw new UnsupportedFeatureException("JDK proxy cannot be created for classes");
	}
}
