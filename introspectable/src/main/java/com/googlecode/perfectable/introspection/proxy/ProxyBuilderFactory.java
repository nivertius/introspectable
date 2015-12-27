package com.googlecode.perfectable.introspection.proxy;

import java.util.ServiceLoader;

public interface ProxyBuilderFactory {
	
	ProxyBuilder<?> ofInterfaces(Class<?>... interfaces);
	
	<I> ProxyBuilder<I> ofInterfacesOf(Class<? extends I> implementingClass);
	
	<I> ProxyBuilder<I> ofInterfaces(Class<I> mainInterface, Class<?>... otherInterfaces);
	
	<I> ProxyBuilder<I> sameAs(I sourceInstance);
	
	final class UnsupportedFeatureException extends RuntimeException {
		private static final long serialVersionUID = -1958070420118962158L;
		
		public UnsupportedFeatureException(String message) {
			super(message);
		}
	}
	
	enum Feature {
		// MARK no features yet
	}
	
	boolean supportsFeature(ProxyBuilderFactory.Feature requestedFeature);
	
	default boolean supportsAllFeatures(ProxyBuilderFactory.Feature... requestedFeatures) {
		for(ProxyBuilderFactory.Feature requestedFeature : requestedFeatures) {
			if(!supportsFeature(requestedFeature)) {
				return false;
			}
		}
		return true;
	}
	
	static ProxyBuilderFactory any() {
		return new JdkProxyBuilderFactory();
	}
	
	static ProxyBuilderFactory withFeature(ProxyBuilderFactory.Feature... requestedFeatures)
			throws ProxyBuilderFactory.UnsupportedFeatureException {
		ServiceLoader<ProxyBuilderFactory> factoryLoader = ServiceLoader.load(ProxyBuilderFactory.class);
		for(ProxyBuilderFactory candidate : factoryLoader) {
			if(candidate.supportsAllFeatures(requestedFeatures)) {
				return candidate;
			}
		}
		throw new UnsupportedFeatureException("No proxy builder factory supports all requested features");
	}
	
}
