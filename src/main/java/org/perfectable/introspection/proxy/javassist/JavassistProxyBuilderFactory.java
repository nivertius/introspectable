package org.perfectable.introspection.proxy.javassist;

import org.perfectable.introspection.proxy.ProxyBuilder;
import org.perfectable.introspection.proxy.ProxyBuilderFactory;

import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import javassist.util.proxy.ProxyFactory;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import static com.google.common.base.Preconditions.checkArgument;

public final class JavassistProxyBuilderFactory implements ProxyBuilderFactory {

	private static final ObjenesisStd OBJENESIS = new ObjenesisStd();

	private static final Set<Feature> SUPPORTED_FEATURES = EnumSet.of(Feature.SUPERCLASS);

	@Override
	public boolean supportsFeature(Feature requestedFeature) {
		return SUPPORTED_FEATURES.contains(requestedFeature);
	}

	@Override
	public ProxyBuilder<?> ofInterfaces(Class<?>... interfaces) {
		checkArgument(interfaces.length > 0);
		Stream.of(interfaces).forEach(ProxyBuilderFactory::checkProxyableInterface);
		ProxyFactory factory = new ProxyFactory();
		factory.setInterfaces(interfaces);
		return createFromFactory(factory);
	}

	@Override
	public <I> ProxyBuilder<I> ofClass(Class<I> sourceClass, Class<?>... additionalInterfaces) {
		checkArgument(!Modifier.isFinal(sourceClass.getModifiers()));
		Stream.of(additionalInterfaces).forEach(ProxyBuilderFactory::checkProxyableInterface);
		if (ProxyFactory.isProxyClass(sourceClass)
				&& Stream.of(additionalInterfaces)
						.allMatch(testedInterface -> testedInterface.isAssignableFrom(sourceClass))) {
			return createFromProxyClass(sourceClass);
		}
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(sourceClass);
		factory.setInterfaces(additionalInterfaces);
		return createFromFactory(factory);
	}

	private static <I> ProxyBuilder<I> createFromFactory(ProxyFactory factory) {
		@SuppressWarnings("unchecked")
		Class<I> proxyClass = factory.createClass();
		return createFromProxyClass(proxyClass);
	}

	private static <I> ProxyBuilder<I> createFromProxyClass(Class<I> proxyClass) {
		ObjectInstantiator<I> instantiator = OBJENESIS.getInstantiatorOf(proxyClass);
		return JavassistProxyBuilder.create(instantiator);
	}

	// constructor must be public, this class is instantiated from ServiceLoader
}
