package org.perfectable.introspection.proxy;

import java.util.Set;
import java.util.stream.Stream;

import com.google.common.collect.ObjectArrays;

import static com.google.common.base.Preconditions.checkArgument;
import static org.perfectable.introspection.Introspections.introspect;

public abstract class AbstractProxyBuilderFactory implements ProxyBuilderFactory {

	private final Set<Feature> supportedFeatures;

	protected AbstractProxyBuilderFactory(Set<Feature> supportedFeatures) {
		this.supportedFeatures = supportedFeatures;
	}

	protected abstract ProxyBuilder<?> ofInterfacesSafe(ClassLoader classLoader, Class<?>... interfaces);

	protected abstract <I> ProxyBuilder<I> ofClassSafe(ClassLoader classLoader,
													   Class<I> sourceClass, Class<?>... additionalInterfaces)
			throws UnsupportedFeatureException;

	@Override
	public ProxyBuilder<?> ofInterfaces(Class<?>... interfaces) {
		checkArgument(interfaces.length > 0);
		Stream.of(interfaces).forEach(AbstractProxyBuilderFactory::checkProxyableInterface);
		ClassLoader classLoader =
				interfaces[0].getClassLoader(); // SUPPRESS we actually want first interface classloader here
		checkClassloader(classLoader, interfaces);
		return ofInterfacesSafe(classLoader, interfaces);
	}

	@Override
	public final <I> ProxyBuilder<I> ofInterfaces(Class<I> mainInterface, Class<?>... otherInterfaces) {
		Class<?>[] usedInterfaces = ObjectArrays.concat(mainInterface, otherInterfaces);
		@SuppressWarnings("unchecked")
		ProxyBuilder<I> casted = (ProxyBuilder<I>) ofInterfaces(usedInterfaces);
		return casted;
	}

	@Override
	public <I> ProxyBuilder<I> ofClass(Class<I> sourceClass, Class<?>... additionalInterfaces)
			throws UnsupportedFeatureException {
		Stream.of(additionalInterfaces).forEach(AbstractProxyBuilderFactory::checkProxyableInterface);
		ClassLoader classLoader = sourceClass.getClassLoader();
		checkClassloader(classLoader, additionalInterfaces);
		return ofClassSafe(classLoader, sourceClass, additionalInterfaces);
	}

	@Override
	public final <I> ProxyBuilder<I> ofInterfacesOf(Class<? extends I> implementingClass, Class<?>... otherInterfaces) {
		Class<?>[] interfaces = introspect(implementingClass).interfaces().stream()
				.toArray(Class<?>[]::new);
		Class<?>[] usedInterfaces = ObjectArrays.concat(interfaces, otherInterfaces, Class.class);
		// this is safe almost always?
		@SuppressWarnings("unchecked")
		ProxyBuilder<I> builder = (ProxyBuilder<I>) ofInterfaces(usedInterfaces);
		return builder;
	}

	@Override
	public final <I> ProxyBuilder<I> ofType(Class<I> type, Class<?>... additionalInterfaces)
			throws UnsupportedFeatureException {
		if (type.isInterface()) {
			return ofInterfaces(type, additionalInterfaces);
		}
		return ofClass(type, additionalInterfaces);
	}

	@Override
	public final <I> ProxyBuilder<I> sameAs(I sourceInstance) throws UnsupportedFeatureException {
		@SuppressWarnings("unchecked")
		Class<I> sourceClass = (Class<I>) sourceInstance.getClass();
		return ofClass(sourceClass);
	}

	@Override
	public final boolean supportsFeature(ProxyBuilderFactory.Feature requestedFeature) {
		return supportedFeatures.contains(requestedFeature);
	}

	private static void checkClassloader(ClassLoader referenceLoader, Class<?>... otherInterfaces) {
		Stream.of(otherInterfaces)
				.forEach(testedInterface -> checkArgument(referenceLoader.equals(testedInterface.getClassLoader())));
	}

	private static void checkProxyableInterface(Class<?> testedInterface) {
		checkArgument(testedInterface.isInterface());
		checkArgument(!testedInterface.isPrimitive());
	}

}
