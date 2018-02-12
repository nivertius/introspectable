package org.perfectable.introspection.proxy;

import org.perfectable.introspection.proxy.ProxyBuilderFactory.Feature;

import java.lang.reflect.Method;
import java.util.Optional;
import javax.annotation.Nullable;

import static org.perfectable.introspection.Introspections.introspect;

public final class LazyInitialization {
	public interface Proxy<T> {
		Optional<T> extractInstance();
	}

	private static final ProxyBuilderFactory PROXY_BUILDER_FACTORY =
			ProxyBuilderFactory.withFeature(Feature.SUPERCLASS);

	@FunctionalInterface
	public interface Initializer<T> {
		T initialize();
	}

	public static <T> T createProxy(Class<T> resultClass, Initializer<? extends T> initializer) {
		LazyInitializationHandler<T> handler = LazyInitializationHandler.create(initializer);
		return PROXY_BUILDER_FACTORY.ofType(resultClass, Proxy.class).instantiate(handler);
	}

	private static final class LazyInitializationHandler<T> implements InvocationHandler<T> {
		private static final Method EXTRACT_INSTANCE_METHOD =
				introspect(Proxy.class).methods().named("extractInstance").parameters().unique();

		private final Initializer<? extends T> initializer;

		@Nullable
		private T instance;

		public static <T> LazyInitializationHandler<T> create(Initializer<? extends T> initializer) {
			return new LazyInitializationHandler<>(initializer);
		}

		private LazyInitializationHandler(Initializer<? extends T> initializer) {
			this.initializer = initializer;
		}

		@Nullable
		@Override
		public Object handle(Invocation<T> invocation) throws Throwable {
			return invocation.proceed((method, receiver, arguments) -> {
				if (EXTRACT_INSTANCE_METHOD.equals(method)) {
					return Optional.ofNullable(this.instance);
				}
				if (this.instance == null) {
					this.instance = this.initializer.initialize();
				}
				if (!method.getDeclaringClass().isAssignableFrom(this.instance.getClass())) {
					throw new AssertionError("Method extracted is of incompatible class");
				}
				return MethodInvocation.of(method, this.instance, arguments).invoke();
			});
		}
	}

	private LazyInitialization() {
		// utility class
	}
}
