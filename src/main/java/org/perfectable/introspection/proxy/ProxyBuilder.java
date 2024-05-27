package org.perfectable.introspection.proxy;

import java.lang.reflect.Modifier;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
public final class ProxyBuilder<I extends @NonNull Object> {
	private ProxyBuilder(@Nullable ClassLoader classLoader, Class<?> baseClass,
						 ImmutableList<? extends Class<?>> interfaces,
						 ImmutableList<InvocationHandler<? extends @Nullable Object, ?,
							 ? super MethodInvocation<I>>> interceptors,
						 ProxyService service) {
		this.classLoader = classLoader;
		this.baseClass = baseClass;
		this.interfaces = interfaces;
		this.interceptors = interceptors;
		this.service = service;
	}

	@SuppressWarnings("Immutable")
	private final @Nullable ClassLoader classLoader;
	private final Class<?> baseClass;
	private final ImmutableList<? extends Class<?>> interfaces;
	@SuppressWarnings("Immutable")
	private final ImmutableList<InvocationHandler<? extends @Nullable Object, ?,
		? super MethodInvocation<I>>> interceptors;
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
	public static <X extends @NonNull Object> ProxyBuilder<X> forClass(Class<X> superclass) {
		checkArgument(!superclass.isInterface());
		checkArgument(!superclass.isPrimitive());
		checkArgument(!Modifier.isFinal(superclass.getModifiers()));
		@Nullable ClassLoader classLoader = superclass.getClassLoader();
		ProxyService service = ProxyService.INSTANCES.get(ProxyService.Feature.SUPERCLASS);
		return new ProxyBuilder<>(classLoader, superclass, ImmutableList.of(),
			ImmutableList.of(), service);
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
	public static <X extends @NonNull Object> ProxyBuilder<X> forInterface(Class<X> baseInterface) {
		checkArgument(baseInterface.isInterface());
		@Nullable ClassLoader classLoader = baseInterface.getClassLoader();
		ProxyService service = ProxyService.INSTANCES.get();
		return new ProxyBuilder<>(classLoader, Object.class, ImmutableList.of(baseInterface),
			ImmutableList.of(), service);
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
	public static <X extends @NonNull Object> ProxyBuilder<X> forType(Class<X> resultClass) {
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
		checkArgument(Objects.equals(classLoader, additionalInterface.getClassLoader()));
		ImmutableList<? extends Class<?>> newInterfaces = ImmutableList.<Class<?>>builder()
			.addAll(interfaces).add(additionalInterface).build();
		return new ProxyBuilder<>(classLoader, baseClass, newInterfaces, interceptors, service);
	}

	/**
	 * Creates proxy builder that will apply specified interceptor to produced proxy.
	 *
	 * <p>Interceptors will be in order they are introduced, i.e. the first one passed to this method for specific proxy
	 * will receive actual call, and will proceed to the next interceptor.
	 *
	 * @param interceptor interceptor to add
	 * @return new proxy builder with added interceptor
	 */
	public ProxyBuilder<I> withInterceptor(
			InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<I>> interceptor) {
		ImmutableList<InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<I>>> newInterceptors =
			ImmutableList.<InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<I>>>builder()
				.add(interceptor).addAll(interceptors).build();
		return new ProxyBuilder<>(classLoader, baseClass, interfaces, newInterceptors, service);
	}

	/**
	 * Creates proxy builder that will apply specified AOP Alliance interceptor to produced proxy.
	 *
	 * <p>Interceptors will be in order they are introduced, i.e. the first one passed to this method for specific proxy
	 * will receive actual call, and will proceed to the next interceptor.
	 *
	 * @param interceptor interceptor to add
	 * @return new proxy builder with added interceptor
	 */
	public ProxyBuilder<I> withAopInterceptor(org.aopalliance.intercept.MethodInterceptor interceptor) {
		return withInterceptor(new MethodInterceptorAdapter<>(interceptor));
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
		return new ProxyBuilder<>(classLoader, baseClass, interfaces, interceptors, newService);
	}

	/**
	 * Creates instance of configured proxy class.
	 *
	 * @param handler method that proxy will delegate its calls to
	 * @return proxy instance
	 */
	public I instantiate(InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<I>> handler) {
		InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<I>> finalHandler =
			joinInterceptors(handler);
		return service.instantiate(classLoader, baseClass, interfaces, finalHandler);
	}

	/**
	 * Creates instance of configured proxy class.
	 *
	 * @param target Object that will receive intercepted calls.
	 * @return proxy instance
	 */
	public I delegateTo(I target) {
		return instantiate(ForwardingHandler.of(target));
	}

	private InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<I>> joinInterceptors(
		InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<I>> handler) {
		InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<I>> current = handler;
		for (InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<I>> wrapper : interceptors) {
			InvocationHandler<? extends @Nullable Object, ?, ? super MethodInvocation<I>> finalCurrent = current;
			current = invocation -> wrapper.handle(new InterceptedMethodInvocation<>(invocation, finalCurrent));
		}
		return current;
	}

}
