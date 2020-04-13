package org.perfectable.introspection.proxy;

import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import com.google.errorprone.annotations.Immutable;

@Immutable
public interface ProxyService {

	Container INSTANCES = new Container();

	boolean supportsFeature(Feature feature);

	default boolean supportsAllFeatures(Feature... features) {
		return Stream.of(features).allMatch(this::supportsFeature);
	}

	<I> I instantiate(ClassLoader classLoader, Class<?> baseClass, List<? extends Class<?>> interfaces,
							  InvocationHandler<? super MethodInvocation<I>> handler)
		throws UnsupportedFeatureException;

	@SuppressWarnings("serial")
	final class UnsupportedFeatureException extends RuntimeException {
		public UnsupportedFeatureException(String message) {
			super(message);
		}
	}

	enum Feature {
		SUPERCLASS
	}

	final class Container {
		private final ServiceLoader<ProxyService> cache = ServiceLoader.load(ProxyService.class);

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
