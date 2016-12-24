package org.perfectable.introspection.proxy;

import org.perfectable.introspection.ReferenceExtractor;

import java.lang.reflect.Method;

import com.google.common.collect.ImmutableMap;

// SUPPRESS NEXT SuppressWarnings
@SuppressWarnings("FunctionalInterfaceClash")
public final class StandardInvocationHandlerBuilder<T> implements InvocationHandlerBuilder<T> {
	private final ReferenceExtractor<T> referenceExtractor;
	private final ImmutableMap<Method, InvocationHandler<? super T>> methods;

	private interface Invocable<T> {
		Object invoke(T receiver, Object... arguments);
	}

	static <T> StandardInvocationHandlerBuilder<T> start(Class<T> sourceClass) {
		ReferenceExtractor<T> referenceExtractor = ReferenceExtractor.of(sourceClass);
		ImmutableMap<Method, InvocationHandler<? super T>> methods = ImmutableMap.of();
		return new StandardInvocationHandlerBuilder<>(referenceExtractor, methods);
	}

	private StandardInvocationHandlerBuilder(ReferenceExtractor<T> referenceExtractor,
											 ImmutableMap<Method, InvocationHandler<? super T>> methods) {
		this.referenceExtractor = referenceExtractor;
		this.methods = methods;
	}

	@Override
	public Binder<T, ParameterlessProcedure<T>> bind(ParameterlessProcedure<? super T> registered) {
		Method method = this.referenceExtractor.extract(registered::execute);
		return executed -> {
			Invocable<T> function = (T receiver, Object... arguments) -> {
				executed.execute(receiver);
				return null;
			};
			return withHandling(method, function);
		};
	}

	@Override
	public <A1> Binder<T, SingleParameterProcedure<T, A1>> bind(SingleParameterProcedure<? super T, A1> registered) {
		Method method = this.referenceExtractor.extract(registered::execute);
		return executed -> {
			@SuppressWarnings("unchecked")
			Invocable<T> function = (T receiver, Object... arguments) -> {
				executed.execute(receiver, (A1) arguments[0]);
				return null;
			};
			return withHandling(method, function);
		};
	}

	@Override
	public <R> Binder<T, ParameterlessFunction<T, R>> bind(ParameterlessFunction<? super T, R> registered) {
		Method method = this.referenceExtractor.extract(registered::execute);
		return executed -> {
			Invocable<T> function = (T receiver, Object... arguments) -> executed.execute(receiver);
			return withHandling(method, function);
		};
	}

	@Override
	public <R, A1> Binder<T, SingleParameterFunction<T, R, A1>> bind(
			SingleParameterFunction<? super T, R, A1> registered) {
		Method method = this.referenceExtractor.extract(registered::execute);
		return executed -> {
			@SuppressWarnings("unchecked")
			Invocable<T> function =
					(T receiver, Object... arguments) -> executed.execute(receiver, (A1) arguments[0]);
			return withHandling(method, function);
		};
	}

	private StandardInvocationHandlerBuilder<T> withHandling(Method invocable, Invocable<T> replacement) {
		InvocationHandler<? super T> handler = (invocation) -> {
			return invocation.proceed((method, receiver, arguments) -> replacement.invoke(receiver, arguments));
		};
		ImmutableMap<Method, InvocationHandler<? super T>> newMethods =
				ImmutableMap.<Method, InvocationHandler<? super T>>builder()
						.putAll(this.methods)
						.put(invocable, handler)
						.build();
		return new StandardInvocationHandlerBuilder<>(this.referenceExtractor, newMethods);
	}

	@Override
	public InvocationHandler<T> build(InvocationHandler<? super T> fallback) {
		return new DispatchingInvocationHandler<>(this.methods, fallback);
	}

	private static final class DispatchingInvocationHandler<T> implements InvocationHandler<T> {
		private final InvocationHandler<? super T> fallback;
		private final ImmutableMap<Method, InvocationHandler<? super T>> methods;

		DispatchingInvocationHandler(ImmutableMap<Method, InvocationHandler<? super T>> methods,
									 InvocationHandler<? super T> fallback) {
			this.methods = methods;
			this.fallback = fallback;
		}

		@Override
		public Object handle(Invocation<T> invocation) throws Throwable {
			return invocation.proceed((method, receiver, arguments) -> {
				@SuppressWarnings("unchecked")
				InvocationHandler<T> handler = (InvocationHandler<T>) methods.getOrDefault(method, this.fallback);
				return handler.handle(invocation);
			});
		}

	}

}
