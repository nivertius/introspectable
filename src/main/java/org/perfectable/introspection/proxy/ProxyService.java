package org.perfectable.introspection.proxy;

import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import com.google.errorprone.annotations.Immutable;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Method of producing proxies.
 *
 * <p>There are multiple ways of creating proxy in Java, from simple {@link java.lang.reflect.Proxy}, through support
 * frameworks like javassist, to class generation libraries like cglib or bytebuddy. This class creates universal facade
 * to potentially all these methods.
 *
 * <p>Different frameworks supports different proxying features, for example, javassist can create proxy classes that
 * extend specific non-final class, not only that implement specific interfaces. These features are represented by
 * {@link Feature} and can be tested for support (using {@link #supportsAllFeatures} or {@link #supportsFeature}).
 *
 * <p>Interface implementation can be registered to be used with {@link ServiceLoader}, and should be obtained using
 * {@link #INSTANCES}.
 *
 * <p>This interface can be implemented externally, and registered with Service Loader to be usable in
 * {@link ProxyBuilder}.
 *
 * @see ProxyBuilder
 */
@Immutable
public interface ProxyService {
	/**
	 * Container managing all instances registered with {@link ServiceLoader}.
	 */
	Container INSTANCES = new Container();

	/**
	 * If the concrete service supports specified feature.
	 *
	 * @param feature feature to test
	 * @return if service supports feature
	 */
	boolean supportsFeature(Feature feature);

	/**
	 * If the concrete service supports all of specified features.
	 *
	 * @param features feature set to test
	 * @return if service supports features
	 */
	default boolean supportsAllFeatures(Feature... features) {
		return Stream.of(features).allMatch(this::supportsFeature);
	}

	/**
	 * Creates proxy instance.
	 *
	 * <p>This method either creates new proxy class, or reuses one that matches. This proxy class extends
	 * {@code baseClass} and implements {@code interfaces}, and is created in {@code classLoader}. After creating this
	 * class, its object is instantiated. The object will delegate every call that it receives to specified
	 * {@link InvocationHandler}. The only exception is {@link Object#finalize}, which must be ignored.
	 *
	 * <p>This method assumes that the arguments were correctly prepared:
	 * <ul>
	 *     <li>{@code baseClass} is not final</li>
	 *     <li>{@code baseClass} is actually a class (not an interface)</li>
	 *     <li>{@code baseClass} and each element of {@code interfaces} has classloader exactly {@code classLoader}</li>
	 *     <li>Each element in {@code interfaces} is an interface (not a class)</li>
	 * </ul>
	 *
	 * @param <I> type of proxy
	 * @param classLoader classloader used to create proxy class
	 * @param baseClass proxy superclass, cannot be final, must be a class
	 * @param interfaces additional interfaces that proxy class should implement
	 * @param handler invocation handler to pass method execution for this proxy instance
	 * @return proxy instance
	 * @throws UnsupportedFeatureException when this service cannot create proxy with requested parameters, because
	 *     it doesn't support some feature required to do so
	 */
	<I> I instantiate(@Nullable ClassLoader classLoader, Class<?> baseClass, List<? extends Class<?>> interfaces,
					  InvocationHandler<?, ?, ? super MethodInvocation<I>> handler)
		throws UnsupportedFeatureException;

	/** Thrown when factory cannot create a builder with requested features. */
	@SuppressWarnings("serial")
	final class UnsupportedFeatureException extends RuntimeException {
		@SuppressWarnings("javadoc")
		public UnsupportedFeatureException(String message) {
			super(message);
		}
	}

	/** Features that service can support. */
	enum Feature {
		/** Service can create proxies that have superclass other than Object. */
		SUPERCLASS
	}

	/**
	 * Container that manages service-loaded instances of {@link ProxyService}.
	 */
	final class Container {
		private final ServiceLoader<ProxyService> cache = ServiceLoader.load(ProxyService.class);

		/**
		 * Extracts proxy service with specified features.
		 *
		 * @param requiredFeatures features that returned service must support
		 * @return proxy service
		 * @throws UnsupportedFeatureException when no proxy service could be found with specified features.
		 */
		public ProxyService get(Feature... requiredFeatures) {
			for (ProxyService service : cache) {
				if (service.supportsAllFeatures(requiredFeatures)) {
					return service;
				}
			}
			throw new UnsupportedFeatureException("No proxy service supports " + Arrays.toString(requiredFeatures));
		}

		@SuppressWarnings("PMD.UnnecessaryConstructor")
		Container() {
			// package-private access
		}
	}
}
