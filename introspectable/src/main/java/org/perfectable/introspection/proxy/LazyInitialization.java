package org.perfectable.introspection.proxy;

import org.perfectable.introspection.Methods;
import org.perfectable.introspection.proxy.ProxyBuilderFactory.Feature;

import java.lang.reflect.Method;
import java.util.Optional;
import javax.annotation.Nullable;

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
		private static final Method EXTRACT_INSTANCE_METHOD = Methods.safeExtract(Proxy.class, "extractInstance");

		private final Initializer<? extends T> initializer;

		@Nullable
		private T instance;

		public static <T> LazyInitializationHandler<T> create(Initializer<? extends T> initializer) {
			return new LazyInitializationHandler<>(initializer);
		}

		private LazyInitializationHandler(Initializer<? extends T> initializer) {
			this.initializer = initializer;
		}

		@Override
		public Object handle(BoundInvocation<? extends T> invocation) throws Throwable {
			@SuppressWarnings("unchecked")
			MethodBoundInvocation<? extends T> methodInvocation = (MethodBoundInvocation<? extends T>) invocation;
			MethodBoundInvocationMappingDecomposer<T> transformer =
					MethodBoundInvocationMappingDecomposer
							.<T>identity()
							.withMethodTransformer(this::methodSelector)
							.withReceiverTransformer(receiver -> this.instance);
			return methodInvocation.decompose(transformer).invoke();
		}

		private Invocable<T> methodSelector(Method method) {
			if (EXTRACT_INSTANCE_METHOD.equals(method)) {
				return (receiver, arguments) -> Optional.ofNullable(this.instance);
			}
			ensureInitialized();
			if (!method.getDeclaringClass().isAssignableFrom(this.instance.getClass())) {
				throw new AssertionError("Method extracted is of incompatibilie class");
			}
			@SuppressWarnings("unchecked")
			Invocable<T> result = (Invocable<T>) MethodInvocable.of(method);
			return result;
		}

		private void ensureInitialized() {
			if (this.instance != null) {
				return;
			}
			this.instance = this.initializer.initialize();
		}
	}

	private LazyInitialization() {
		// utility class
	}
}
