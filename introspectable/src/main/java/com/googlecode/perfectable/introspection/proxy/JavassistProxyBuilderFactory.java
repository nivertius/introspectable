package com.googlecode.perfectable.introspection.proxy;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.Set;

import javassist.util.proxy.ProxyFactory;

import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

public class JavassistProxyBuilderFactory implements ProxyBuilderFactory {
	
	private static final ObjenesisStd OBJENESIS = new ObjenesisStd();
	
	public static final JavassistProxyBuilderFactory INSTANCE = new JavassistProxyBuilderFactory();
	
	private static final Set<Feature> SUPPORTED_FEATURES = EnumSet.of(Feature.SUPERCLASS);
	
	@Override
	public boolean supportsFeature(Feature requestedFeature) {
		return SUPPORTED_FEATURES.contains(requestedFeature);
	}
	
	@Override
	public ProxyBuilder<?> ofInterfaces(Class<?>... interfaces) {
		ProxyFactory factory = new ProxyFactory();
		factory.setInterfaces(interfaces);
		return createFromFactory(factory);
	}
	
	@Override
	public <I> ProxyBuilder<I> ofClass(Class<I> sourceClass) {
		checkArgument(!Modifier.isFinal(sourceClass.getModifiers()));
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(sourceClass);
		return createFromFactory(factory);
	}
	
	private static <I> ProxyBuilder<I> createFromFactory(ProxyFactory factory) {
		@SuppressWarnings("unchecked")
		Class<I> proxyClass = factory.createClass();
		ObjectInstantiator<I> instantiator = OBJENESIS.getInstantiatorOf(proxyClass);
		return JavassistProxyBuilder.create(instantiator);
	}
}
