package org.perfectable.introspection.proxy;

import java.lang.reflect.Method;
import java.util.Optional;
import javax.annotation.Nullable;

import com.google.errorprone.annotations.concurrent.LazyInit;

import static org.perfectable.introspection.Introspections.introspect;

/**
 * Helper class to create proxy which delegate invocation to lazily-initialized instance.
 *
 * <p>Proxies are created by {@link #createProxy}.
 */
public final class LazyInitialization {
	/**
	 * Additional interface that all proxies created by {@link #createProxy} will implement.
	 *
	 * @param <T> type of actual delegation target.
	 */
	public interface Proxy<T> {
		/**
		 * Extracts delegation target.
		 *
		 * @return target if it was initialized, empty if proxy hasn't yet been initialized.
		 */
		Optional<T> extractInstance();
	}

	/**
	 * Provider interface for proxy delegation target.
	 *
	 * @param <T> type of target
	 */
	@FunctionalInterface
	public interface Initializer<T> {
		/**
		 * Initializes the actual target and returns it.
		 *
		 * <p>In ideal scenario this method is called once for each proxy.
		 *
		 * @return Delegation target for created proxy
		 */
		T initialize();
	}

	/**
	 * Creates a proxy that forwards invocations to lazily initialized instance.
	 *
	 * <p>On any method call on resulting proxy it will either initialize instance and cache it, or use existing
	 * instance, and delegate the call to it, returning the result or rethrowing exception of the call. There are
	 * two exceptions of this rule:
	 * <ul>
	 *     <li>{@link Object#finalize} is always ignored when called on proxy</li>
	 *     <li>Methods from {@link Proxy} interface are treated specially and handled with separate route. They
	 *     won't necessarily initialize the proxy and won't be forwarded to the target.</li>
	 * </ul>
	 *
	 * <p>Proxies will have {@code resultClass} as a superclass/superinterface. Additionally, proxy class will implement
	 * {@link Proxy} interface. This wont be reflected in the compile-time type, but it can be checked by instanceof
	 * and it can be casted to it.
	 *
	 * @param resultClass class or interface that proxy has to implement. This is also a superclass/superinterface of
	 *     initialized instance
	 * @param initializer method of initializing actual delegation target
	 * @param <T> type of proxy
	 * @return proxy object
	 */
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
				@SuppressWarnings("unused") @Nullable T receiver,
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
