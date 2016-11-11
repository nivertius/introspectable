package org.perfectable.introspection.proxy;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;

import org.perfectable.introspection.ReferenceExtractor;

import com.google.common.collect.ImmutableMap;

@SuppressWarnings("FunctionalInterfaceClash")
public final class StandardInvocationHandlerBuilder<T> implements InvocationHandlerBuilder<T> {
	private final ReferenceExtractor<T> referenceExtractor;
	private final ImmutableMap<Method, InvocationHandler<? super T>> methods;
	
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
		Method method = this.referenceExtractor.extractNone(registered::execute);
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
		Method method = this.referenceExtractor.extractSingle(registered::execute);
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
		Method method = this.referenceExtractor.extractNone(registered::execute);
		return executed -> {
				Invocable<T> function = (T receiver, Object... arguments) -> executed.execute(receiver);
				return withHandling(method, function);
		};
	}

	@Override
	public <R, A1> Binder<T, SingleParameterFunction<T, R, A1>> bind(
			SingleParameterFunction<? super T, R, A1> registered) {
		Method method = this.referenceExtractor.extractSingle(registered::execute);
		return executed -> {
			@SuppressWarnings("unchecked")
			Invocable<T> function =
					(T receiver, Object... arguments) -> executed.execute(receiver, (A1) arguments[0]);
			return withHandling(method, function);
		};
	}
	
	private StandardInvocationHandlerBuilder<T> withHandling(Method invocable, Invocable<T> replacement) {
		MethodBoundInvocationMappingDecomposer<T> decomposer =
				MethodBoundInvocationMappingDecomposer.<T> identity().withMethodTransformer(method -> replacement);
		InvocationHandler<T> handler = invocation -> {
			@SuppressWarnings("unchecked")
			MethodBoundInvocation<T> methodInvocation = (MethodBoundInvocation<T>) invocation;
			return methodInvocation.decompose(decomposer).invoke();
		};
		ImmutableMap<Method, InvocationHandler<? super T>> newMethods =
				ImmutableMap.<Method, InvocationHandler<? super T>> builder().putAll(this.methods).put(invocable, handler)
						.build();
		return new StandardInvocationHandlerBuilder<>(this.referenceExtractor, newMethods);
	}
	
	@Override
	public InvocationHandler<T> build(InvocationHandler<? super T> fallback) {
		return new DispatchingInvocationHandler<>(this.methods, fallback);
	}
	
	private static final class DispatchingInvocationHandler<T> implements InvocationHandler<T> {
		
		private final class MethodDispatchingDecomposer implements
				MethodBoundInvocation.Decomposer<InvocationHandler<? super T>, T> {
			private InvocationHandler<? super T> handler;
			
			@Override
			public void method(Method method) {
				this.handler = DispatchingInvocationHandler.this.methods.getOrDefault(method,
						DispatchingInvocationHandler.this.fallback);
			}
			
			@Override
			public void receiver(T receiver) {
				// ignored
			}
			
			@Override
			public <X> void argument(int index, Class<? super X> formal, X actual) {
				// ignored
			}
			
			@Override
			public InvocationHandler<? super T> finish() {
				checkNotNull(this.handler);
				return this.handler;
			}
		}
		
		private final InvocationHandler<? super T> fallback;
		private final ImmutableMap<Method, InvocationHandler<? super T>> methods;
		
		DispatchingInvocationHandler(ImmutableMap<Method, InvocationHandler<? super T>> methods,
				InvocationHandler<? super T> fallback) {
			this.methods = methods;
			this.fallback = fallback;
		}
		
		@Override
		public Object handle(BoundInvocation<? extends T> invocation) throws Throwable {
			MethodBoundInvocation<? extends T> methodInvocation = (MethodBoundInvocation<? extends T>) invocation;
			InvocationHandler<? super T> handler = methodInvocation.decompose(new MethodDispatchingDecomposer());
			return handler.handle(invocation);
		}
		
	}
	
}
