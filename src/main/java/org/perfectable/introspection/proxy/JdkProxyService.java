package org.perfectable.introspection.proxy;

import org.perfectable.introspection.ObjectMethods;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.google.auto.service.AutoService;
import com.google.errorprone.annotations.Immutable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

// THIS IS NOT A PUBLIC API: class must be public because of ServiceLoader
@SuppressWarnings("javadoc")
@Immutable
@AutoService(ProxyService.class)
public final class JdkProxyService implements ProxyService {

	private static final Set<Feature> SUPPORTED_FEATURES = EnumSet.noneOf(Feature.class);
	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	@Override
	public boolean supportsFeature(Feature feature) {
		return SUPPORTED_FEATURES.contains(feature);
	}

	@Override
	public <I> I instantiate(@Nullable ClassLoader classLoader, Class<?> baseClass, List<? extends Class<?>> interfaces,
							 InvocationHandler<? super MethodInvocation<I>> handler)
		throws UnsupportedFeatureException {
		if (!baseClass.getName().equals(Object.class.getName())) {
			throw new UnsupportedFeatureException("JDK proxy cannot be created with superclass other than Object");
		}
		java.lang.reflect.InvocationHandler adapterHandler = JdkInvocationHandlerAdapter.adapt(handler);
		Class<?>[] interfacesArray = (@NonNull Class<?>[]) interfaces.toArray(EMPTY_CLASS_ARRAY);
		try {
			@SuppressWarnings({"unchecked", "argument.type.incompatible"})
			I instance = (@NonNull I) Proxy.newProxyInstance(classLoader, interfacesArray, adapterHandler);
			return instance;
		}
		catch (IllegalArgumentException e) {
			throw new AssertionError("Proxy construction failed", e);
		}
	}

	private static final class JdkInvocationHandlerAdapter<T> implements java.lang.reflect.InvocationHandler {
		private final InvocationHandler<? super MethodInvocation<T>> handler;

		static <T> JdkInvocationHandlerAdapter<T> adapt(InvocationHandler<? super MethodInvocation<T>> handler) {
			return new JdkInvocationHandlerAdapter<>(handler);
		}

		private JdkInvocationHandlerAdapter(InvocationHandler<? super MethodInvocation<T>> handler) {
			this.handler = handler;
		}

		@SuppressWarnings("override.return.invalid")
		@Override
		public @Nullable Object invoke(Object proxy, Method method,
							 @Nullable Object @Nullable [] args)
			throws Throwable {
			requireNonNull(method);
			if (method.equals(ObjectMethods.FINALIZE)) {
				return null; // ignore proxy finalization
			}
			@SuppressWarnings("unchecked")
			@Nullable T castedProxy = (T) proxy;
			MethodInvocation<T> invocation = MethodInvocation.intercepted(method, castedProxy, args);
			return this.handler.handle(invocation);
		}

	}

}
