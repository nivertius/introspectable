package org.perfectable.introspection.proxy;

import java.lang.reflect.Modifier;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;

import static com.google.common.base.Preconditions.checkArgument;

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

	public static <X> ProxyBuilder<X> forClass(Class<X> superclass) {
		checkArgument(!superclass.isInterface());
		checkArgument(!superclass.isPrimitive());
		checkArgument(!Modifier.isFinal(superclass.getModifiers()));
		ClassLoader classLoader = superclass.getClassLoader();
		ProxyService service = ProxyService.INSTANCES.get(ProxyService.Feature.SUPERCLASS);
		return new ProxyBuilder<X>(classLoader, superclass, ImmutableList.of(), service);
	}

	public static <X> ProxyBuilder<X> forInterface(Class<X> baseInterface) {
		checkArgument(baseInterface.isInterface());
		ClassLoader classLoader = baseInterface.getClassLoader();
		ProxyService service = ProxyService.INSTANCES.get();
		return new ProxyBuilder<>(classLoader, Object.class, ImmutableList.of(baseInterface), service);
	}

	public static <X> ProxyBuilder<X> forType(Class<X> resultClass) {
		if (resultClass.isInterface()) {
			return forInterface(resultClass);
		}
		return forClass(resultClass);
	}

	public ProxyBuilder<I> withInterface(Class<?> additionalInterface) {
		checkArgument(additionalInterface.isInterface());
		checkArgument(classLoader.equals(additionalInterface.getClassLoader()));
		ImmutableList<? extends Class<?>> newInterfaces = ImmutableList.<Class<?>>builder()
			.addAll(interfaces).add(additionalInterface).build();
		return new ProxyBuilder<>(classLoader, baseClass, newInterfaces, service);
	}

	public ProxyBuilder<I> usingService(ProxyService newService) {
		return new ProxyBuilder<>(classLoader, baseClass, interfaces, newService);
	}

	public I instantiate(InvocationHandler<? super MethodInvocation<I>> handler) {
		return service.instantiate(classLoader, baseClass, interfaces, handler);
	}
}
