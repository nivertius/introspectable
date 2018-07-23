package org.perfectable.introspection.proxy.jdk;

import org.perfectable.introspection.proxy.AbstractProxyBuilderFactory;
import org.perfectable.introspection.proxy.ProxyBuilder;
import org.perfectable.introspection.proxy.ProxyBuilderFactory;

import java.util.EnumSet;
import java.util.Set;

import com.google.auto.service.AutoService;

@AutoService(ProxyBuilderFactory.class)
public final class JdkProxyBuilderFactory extends AbstractProxyBuilderFactory {

	private static final Set<Feature> SUPPORTED_FEATURES = EnumSet.noneOf(Feature.class);

	public JdkProxyBuilderFactory() {
		// constructor must be public, this class is instantiated from ServiceLoader
		super(SUPPORTED_FEATURES);
	}

	@Override
	public ProxyBuilder<?> ofInterfacesSafe(ClassLoader classLoader, Class<?>... interfaces) {
		return JdkProxyBuilder.of(classLoader, interfaces);
	}

	@Override
	protected <I> ProxyBuilder<I> ofClassSafe(ClassLoader classLoader,
											  Class<I> sourceClass, Class<?>... additionalInterfaces)
			throws UnsupportedFeatureException {
		throw new UnsupportedFeatureException("JDK proxy cannot be created for classes");
	}
}
