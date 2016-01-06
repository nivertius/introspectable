package com.googlecode.perfectable.introspection.proxy;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ServiceLoader;
import java.util.stream.Stream;

import com.google.common.collect.ObjectArrays;
import com.googlecode.perfectable.introspection.Introspection;

public interface ProxyBuilderFactory {
	
	ProxyBuilder<?> ofInterfaces(Class<?>... interfaces);
	
	default <I> ProxyBuilder<I> ofInterfacesOf(Class<? extends I> implementingClass) {
		Class<?>[] interfaces = Introspection.of(implementingClass).interfaces().stream()
				.toArray(Class[]::new);
		// MARK this is safe almost always?
		@SuppressWarnings("unchecked")
		ProxyBuilder<I> builder = (ProxyBuilder<I>) ofInterfaces(interfaces);
		return builder;
	}
	
	default <I> ProxyBuilder<I> ofInterfaces(Class<I> mainInterface, Class<?>... otherInterfaces) {
		Class<?>[] usedInterfaces = ObjectArrays.concat(mainInterface, otherInterfaces);
		@SuppressWarnings("unchecked")
		ProxyBuilder<I> casted = (ProxyBuilder<I>) ofInterfaces(usedInterfaces);
		return casted;
	}
	
	default <I> ProxyBuilder<I> ofType(Class<I> type, Class<?>... additionalInterfaces)
			throws UnsupportedFeatureException {
		if(type.isInterface()) {
			return ofInterfaces(type, additionalInterfaces);
		}
		return ofClass(type, additionalInterfaces);
	}
	
	<I> ProxyBuilder<I> ofClass(Class<I> sourceClass, Class<?>... additionalInterfaces)
			throws UnsupportedFeatureException;
	
	default <I> ProxyBuilder<I> sameAs(I sourceInstance) throws UnsupportedFeatureException {
		@SuppressWarnings("unchecked")
		Class<I> sourceClass = (Class<I>) sourceInstance.getClass();
		return ofClass(sourceClass);
	}
	
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
	
	static void checkClassloader(final ClassLoader referenceLoader, Class<?>... otherInterfaces) {
		Stream.of(otherInterfaces)
				.forEach(i -> checkArgument(referenceLoader.equals(i.getClassLoader())));
	}
	
	static void checkProxyableInterface(Class<?> testedInterface) {
		checkArgument(testedInterface.isInterface());
		checkArgument(!testedInterface.isPrimitive());
	}
	
}
