package org.perfectable.introspection.proxy;

import java.lang.reflect.Modifier;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Builder pattern for proxies.
 *
 * <p>This class allows chained and intuitive creation of proxy instances. It uses {@link ProxyService} to actually
 * create proxies, and loads required service automatically.
 *
 * @see ProxyService
 * @param <I> type of proxies built
 */
@Immutable
public final class ProxyBuilder<I> {
	private ProxyBuilder(ClassLoader classLoader, Class<?> baseClass,
						 ImmutableList<? extends Class<?>> interfaces, ProxyService service) {
		this.classLoader = classLoader;
		this.baseClass = baseClass;
		this.interfaces = interfaces;
		this.service = service;
	}

	@SuppressWarnings("Immutable")
	private final ClassLoader classLoader;
	private final Class<?> baseClass;
	private final ImmutableList<? extends Class<?>> interfaces;
	private final ProxyService service;

	/**
	 * Creates new builder for class that is actually a class (not an interface).
	 *
	 * <p>This class will be used as a superclass of a proxy class. The class must not be final or primitive.
	 *
	 * <p>This builder will use service that supports building proxies with superclasses.
	 *
	 * @param superclass class to be used as superclass for a proxy
	 * @param <X> type of proxy
	 * @return new proxy builder with specified class
	 */
	public static <X> ProxyBuilder<X> forClass(Class<X> superclass) {
		checkArgument(!superclass.isInterface());
		checkArgument(!superclass.isPrimitive());
		checkArgument(!Modifier.isFinal(superclass.getModifiers()));
		ClassLoader classLoader = superclass.getClassLoader();
		ProxyService service = ProxyService.INSTANCES.get(ProxyService.Feature.SUPERCLASS);
		return new ProxyBuilder<X>(classLoader, superclass, ImmutableList.of(), service);
	}

	/**
	 * Creates new builder for interface.
	 *
	 * <p>This interface will be implemented by proxy class. Proxy class will not have any specific superclass, it will
	 * probably be {@link Object}.
	 *
	 * @param baseInterface interface that will be implemented by proxy class
	 * @param <X> type of proxy
	 * @return new proxy builder with specified interface
	 */
	public static <X> ProxyBuilder<X> forInterface(Class<X> baseInterface) {
		checkArgument(baseInterface.isInterface());
		ClassLoader classLoader = baseInterface.getClassLoader();
		ProxyService service = ProxyService.INSTANCES.get();
		return new ProxyBuilder<>(classLoader, Object.class, ImmutableList.of(baseInterface), service);
	}

	/**
	 * Creates new builder for specified type.
	 *
	 * <p>Depending on if {@code resultClass} is an interface or not, this method will behave either as
	 * {@link #forInterface} or {@link #forClass}
	 *
	 * @param resultClass base proxy type
	 * @param <X> type of proxy
	 * @return new proxy builder with specified interface
	 */
	public static <X> ProxyBuilder<X> forType(Class<X> resultClass) {
		if (resultClass.isInterface()) {
			return forInterface(resultClass);
		}
		return forClass(resultClass);
	}

	/**
	 * Creates proxy builder that have additional interface added to the proxy class.
	 *
	 * @param additionalInterface added interface
	 * @return new proxy builder with additional interface
	 */
	public ProxyBuilder<I> withInterface(Class<?> additionalInterface) {
		checkArgument(additionalInterface.isInterface());
		checkArgument(classLoader.equals(additionalInterface.getClassLoader()));
		ImmutableList<? extends Class<?>> newInterfaces = ImmutableList.<Class<?>>builder()
			.addAll(interfaces).add(additionalInterface).build();
		return new ProxyBuilder<>(classLoader, baseClass, newInterfaces, service);
	}

	/**
	 * Creates proxy builder that have proxy service replaced.
	 *
	 * <p>This method is optional, and needed only if specific service needs to be used. ProxyBuilder will normally
	 * select suitable service.
	 *
	 * @param newService proxy service to use for creating services
	 * @return new proxy builder with replaced service
	 */
	public ProxyBuilder<I> usingService(ProxyService newService) {
		return new ProxyBuilder<>(classLoader, baseClass, interfaces, newService);
	}

	/**
	 * Creates instance of configured proxy class.
	 *
	 * @param handler method that proxy will delegate its calls to
	 * @return proxy instance
	 */
	public I instantiate(InvocationHandler<? super MethodInvocation<I>> handler) {
		return service.instantiate(classLoader, baseClass, interfaces, handler);
	}
}
