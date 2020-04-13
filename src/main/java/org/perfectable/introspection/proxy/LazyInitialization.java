package org.perfectable.introspection.proxy;

import java.lang.reflect.Method;
import java.util.Optional;
import javax.annotation.Nullable;

import com.google.errorprone.annotations.concurrent.LazyInit;

import static org.perfectable.introspection.Introspections.introspect;

public final class LazyInitialization {
	public interface Proxy<T> {
		Optional<T> extractInstance();
	}

	@FunctionalInterface
	public interface Initializer<T> {
		T initialize();
	}

	public static <T> T createProxy(Class<T> resultClass, Initializer<? extends T> initializer) {
		LazyInitializationHandler<T> handler = LazyInitializationHandler.create(initializer);
		return ProxyBuilder.forType(resultClass).withInterface(Proxy.class).instantiate(handler);
	}

	private static final class LazyInitializationHandler<T> implements InvocationHandler<MethodInvocation<T>> {
		private static final Method EXTRACT_INSTANCE_METHOD =
				introspect(Proxy.class).methods().named("extractInstance").parameters().unique();

		private final Initializer<? extends T> initializer;

		@LazyInit
		private transient T instance;

		public static <T> LazyInitializationHandler<T> create(Initializer<? extends T> initializer) {
			return new LazyInitializationHandler<>(initializer);
		}

		private LazyInitializationHandler(Initializer<? extends T> initializer) {
			this.initializer = initializer;
		}

		@Nullable
		@Override
		public Object handle(MethodInvocation<T> invocation) throws Throwable {
			return invocation.decompose(this::replaceInvocation).invoke();
		}

		private Invocation replaceInvocation(Method method,
				@SuppressWarnings("unused") @Nullable T receiver, // SUPPRESS AnnotationLocation
				Object... arguments) {
			if (EXTRACT_INSTANCE_METHOD.equals(method)) {
				return () -> Optional.ofNullable(instance);
			}
			if (this.instance == null) {
				this.instance = this.initializer.initialize();
			}
			return MethodInvocation.of(method, this.instance, arguments);
		}
	}

	private LazyInitialization() {
		// utility class
	}
}
