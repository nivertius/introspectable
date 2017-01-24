package org.perfectable.introspection.proxy;

import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface ProxyBuilderFactory {

	ProxyBuilder<?> ofInterfaces(Class<?>... interfaces);

	<I> ProxyBuilder<I> ofInterfaces(Class<I> mainInterface, Class<?>... otherInterfaces);

	<I> ProxyBuilder<I> ofInterfacesOf(Class<? extends I> implementingClass, Class<?>... otherInterfaces);

	<I> ProxyBuilder<I> ofType(Class<I> type, Class<?>... additionalInterfaces);

	<I> ProxyBuilder<I> ofClass(Class<I> sourceClass, Class<?>... additionalInterfaces)
			throws UnsupportedFeatureException;

	<I> ProxyBuilder<I> sameAs(I sourceInstance)
			throws UnsupportedFeatureException;

	final class UnsupportedFeatureException extends RuntimeException {
		private static final long serialVersionUID = -1958070420118962158L;

		public UnsupportedFeatureException(String message) {
			super(message);
		}
	}

	enum Feature {
		SUPERCLASS
	}

	boolean supportsFeature(ProxyBuilderFactory.Feature requestedFeature);

	default boolean supportsAllFeatures(ProxyBuilderFactory.Feature... requestedFeatures) {
		return Stream.of(requestedFeatures).allMatch(this::supportsFeature);
	}

	static ProxyBuilderFactory any() {
		return new org.perfectable.introspection.proxy.jdk.JdkProxyBuilderFactory();
	}

	static ProxyBuilderFactory withFeature(ProxyBuilderFactory.Feature... requestedFeatures)
			throws ProxyBuilderFactory.UnsupportedFeatureException {
		ServiceLoader<ProxyBuilderFactory> factoryLoader = ServiceLoader.load(ProxyBuilderFactory.class);
		for (ProxyBuilderFactory candidate : factoryLoader) {
			if (candidate.supportsAllFeatures(requestedFeatures)) {
				return candidate;
			}
		}
		throw new UnsupportedFeatureException("No proxy builder factory supports all requested features");
	}

}
