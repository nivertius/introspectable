package org.perfectable.introspection.proxy.javassist;

import org.perfectable.introspection.proxy.AbstractProxyBuilderFactory;
import org.perfectable.introspection.proxy.ProxyBuilder;

import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import javassist.util.proxy.ProxyFactory;
import org.objenesis.ObjenesisStd;
import org.objenesis.instantiator.ObjectInstantiator;

import static com.google.common.base.Preconditions.checkArgument;

public final class JavassistProxyBuilderFactory extends AbstractProxyBuilderFactory {

	private static final ObjenesisStd OBJENESIS = new ObjenesisStd();

	private static final Set<Feature> SUPPORTED_FEATURES = EnumSet.of(Feature.SUPERCLASS);

	public JavassistProxyBuilderFactory() {
		// constructor must be public, this class is instantiated from ServiceLoader
		super(SUPPORTED_FEATURES);
	}

	@Override
	public ProxyBuilder<?> ofInterfacesSafe(ClassLoader classLoader, Class<?>... interfaces) {
		ProxyFactory factory = new ProxyFactory();
		factory.setInterfaces(interfaces);
		return createFromFactory(factory);
	}

	@Override
	public <I> ProxyBuilder<I> ofClassSafe(ClassLoader classLoader,
										   Class<I> sourceClass, Class<?>... additionalInterfaces) {
		checkArgument(!Modifier.isFinal(sourceClass.getModifiers()));
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
}
