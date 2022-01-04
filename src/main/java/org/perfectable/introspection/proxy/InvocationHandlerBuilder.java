package org.perfectable.introspection.proxy;

import org.perfectable.introspection.FunctionalReference;

import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Builder pattern for {@link InvocationHandler}.
 *
 * <p>Handler is built by binding methods to other invocation handlers, so that when single method is called, configured
 * handler is executed. When no handler was assigned to invoked method, a fallback is called, which by default throws.
 *
 * @param <T> type of proxies supported
 */
public final class InvocationHandlerBuilder<T> {
	private final Map<Method, InvocationHandler<?, ?, MethodInvocation<T>>> mapping;
	private final InvocationHandler<?, ?, MethodInvocation<T>> fallback;

	/**
	 * Creates empty invocation handler builder.
	 *
	 * <p>This builder will create handlers that handles invocations by throwing {@code UnsupportedOperationException}.
	 *
	 * @param <T> Receiver type for intercepted methods
	 * @return Unconfigured builder
	 */
	public static <T> InvocationHandlerBuilder<T> create() {
		InvocationHandler<?, ?, MethodInvocation<T>> fallback = invocation -> {
			throw new UnsupportedOperationException(); // SUPPRESS JavadocMethod
		};
		return new InvocationHandlerBuilder<T>(ImmutableMap.of(), fallback);
	}

	private InvocationHandlerBuilder(Map<Method, InvocationHandler<?, ?, MethodInvocation<T>>> mapping,
									 InvocationHandler<?, ?, MethodInvocation<T>> fallback) {
		this.mapping = mapping;
		this.fallback = fallback;
	}

	/**
	 * Starts configuration for binding of instance method without result and no arguments.
	 *
	 * @param reference instance method reference that will be bound
	 * @param <X> type of exception thrown by method
	 * @return Binder that will produce partially configured builder with specified method configured.
	 */
	@SuppressWarnings("FunctionalInterfaceClash")
	public <X extends Exception>
		Binder.Replacing<T, Signatures.Procedure1<T, X>> bind(Signatures.Procedure1<T, X> reference) {
		return bindReplacement(reference);
	}

	/**
	 * Starts configuration for binding of instance method without result and one arguments.
	 *
	 * @param reference instance method reference that will be bound
	 * @param <X> type of exception thrown by method
	 * @param <P1> type of first method parameter
	 * @return Binder that will produce partially configured builder with specified method configured.
	 */
	@SuppressWarnings("FunctionalInterfaceClash")
	public <P1, X extends Exception>
		Binder.Replacing<T, Signatures.Procedure2<T, P1, X>> bind(Signatures.Procedure2<T, P1, X> reference) {
		return bindReplacement(reference);
	}

	/**
	 * Starts configuration for binding of instance method with result and one arguments.
	 *
	 * @param reference instance method reference that will be bound
	 * @param <X> type of exception thrown by method
	 * @param <R> type of method result
	 * @return Binder that will produce partially configured builder with specified method configured.
	 */
	@SuppressWarnings("FunctionalInterfaceClash")
	public <R, X extends Exception>
		Binder.Replacing<T, Signatures.Function1<R, T, X>> bind(Signatures.Function1<R, T, X> reference) {
		return bindReplacement(reference);
	}


	/**
	 * Starts configuration for binding of instance method with result and one arguments.
	 *
	 * @param reference instance method reference that will be bound
	 * @param <X> type of exception thrown by method
	 * @param <P1> type of first method parameter
	 * @param <R> type of method result
	 * @return Binder that will produce partially configured builder with specified method configured.
	 */
	@SuppressWarnings("FunctionalInterfaceClash")
	public <R, P1, X extends Exception>
		Binder.Replacing<T, Signatures.Function2<R, T, P1, X>> bind(Signatures.Function2<R, T, P1, X> reference) {
		return bindReplacement(reference);
	}

	/**
	 * Starts configuration for binding of specified method.
	 *
	 * @param method method to be bound
	 * @return Binder that will produce partially configured builder with specified method configured.
	 */
	public Binder<T> bindMethod(Method method) {
		return replacement -> delegateTo(method, replacement);
	}

	/**
	 * Configures fallback handler that is called when method with no binding was called.
	 *
	 * @param newFallback invocation handler to be called as callback
	 * @return Builder with same configuration, but with specified callback.
	 */
	public InvocationHandlerBuilder<T> withFallback(InvocationHandler<?, ?, MethodInvocation<T>> newFallback) {
		return new InvocationHandlerBuilder<>(mapping, newFallback);
	}

	/**
	 * Builds invocation handler as configured by this builder.
	 *
	 * @return Invocation handler configured from this builder
	 */
	public InvocationHandler<?, ?, MethodInvocation<T>> build() {
		return new InvocationHandler<@Nullable Object, Exception, MethodInvocation<T>>() {
			@Override
			public @Nullable Object handle(MethodInvocation<T> invocation) throws Exception {
				InvocationHandler<?, ?, MethodInvocation<T>> replacement =
					invocation.decompose((method, receiver, arguments) -> mapping.getOrDefault(method, fallback));
				return replacement.handle(invocation);
			}
		};
	}

	/**
	 * Instantiates proxy with configured invocation handler.
	 *
	 * @param proxyClass class of proxy to instantiate
	 * @return proxy instance backed by built handler
	 */
	public T instantiate(Class<T> proxyClass) {
		InvocationHandler<?, ?, MethodInvocation<T>> handler = build();
		return ProxyBuilder.forType(proxyClass).instantiate(handler);
	}

	@SuppressWarnings("PMD.UnusedPrivateMethod") // false positive
	private <R extends Signatures.HeadCurryable<T, ? extends Signatures.InvocationConvertible<?, ?>>
					& FunctionalReference>
	Binder.Replacing<T, R> bindReplacement(R target) {
		Method method = target.introspect().referencedMethod();
		return replacement -> delegateTo(method, replacement);
	}

	private InvocationHandlerBuilder<T> delegateTo(Method method,
												   InvocationHandler<?, ?, MethodInvocation<T>> handler) {
		Map<Method, InvocationHandler<?, ?, MethodInvocation<T>>> newMapping =
			ImmutableMap.<Method, InvocationHandler<?, ?, MethodInvocation<T>>>builder()
				.putAll(mapping).put(method, handler).build();
		return new InvocationHandlerBuilder<T>(newMapping, fallback);
	}

	/**
	 * Configures binding between already selected method to specified invocation handler.
	 *
	 * @param <T> type of proxy
	 */
	public interface Binder<T> {
		/**
		 * Binds method invocation to be handled by specific handler.
		 *
		 * @param handler handler to execute when method is called
		 * @return Invocation handler builder with configured method
		 */
		InvocationHandlerBuilder<T> to(InvocationHandler<?, ?, MethodInvocation<T>> handler);

		/**
		 * Subtype of {@link Binder} which also allows binding to handler that is matched with bound method, passed
		 * as a lambda or method reference.
		 *
		 * @param <T> type of proxy
		 * @param <F> method signature to be accepted
		 */
		interface Replacing<T, F extends Signatures.HeadCurryable<T, ? extends Signatures.InvocationConvertible<?, ?>>>
			extends Binder<T> {

			/**
			 * Configures builder to use specified replacement execution when matching method is called.
			 *
			 * @param replacement code to be called when matching method is invoked
			 * @return Invocation handler builder with configured method
			 */
			default InvocationHandlerBuilder<T> as(F replacement) {
				InvocationHandler<@Nullable Object, Exception, MethodInvocation<T>> handler = invocation -> {
					Invocation<?, ?> decompose = invocation.decompose((method, receiver, arguments) -> {
						Signatures.InvocationConvertible<?, ?> curried = replacement.curryHead(receiver);
						return curried.toInvocation(arguments);
					});
					return decompose.invoke();
				};
				return to(handler);
			}
		}
	}

}
