package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.Method;

import com.google.common.collect.ImmutableMap;
import com.googlecode.perfectable.introspection.ReferenceExtractor;
import com.googlecode.perfectable.introspection.proxy.BoundInvocation.FunctionalInvocation;

public class StandardInvocationHandlerBuilder<T> implements InvocationHandlerBuilder<T> {
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
		return new Binder<T, ParameterlessProcedure<T>>() {
			@Override
			public StandardInvocationHandlerBuilder<T> to(ParameterlessProcedure<T> executed) {
				FunctionalInvocation<T> function = (T receiver, Object... arguments) -> {
					executed.execute(receiver);
					return null;
				};
				return withHandling(method, function);
			}
		};
	}
	
	@Override
	public <A1> Binder<T, SingleParameterProcedure<T, A1>> bind(SingleParameterProcedure<? super T, A1> registered) {
		Method method = this.referenceExtractor.extractSingle(registered::execute);
		return new Binder<T, SingleParameterProcedure<T, A1>>() {
			@Override
			public StandardInvocationHandlerBuilder<T> to(SingleParameterProcedure<T, A1> executed) {
				@SuppressWarnings("unchecked")
				FunctionalInvocation<T> function = (T receiver, Object... arguments) -> {
					executed.execute(receiver, (A1) arguments[0]);
					return null;
				};
				return withHandling(method, function);
			}
		};
	}
	
	@Override
	public <R> Binder<T, ParameterlessFunction<T, R>> bind(ParameterlessFunction<? super T, R> registered) {
		Method method = this.referenceExtractor.extractNone(registered::execute);
		return new Binder<T, ParameterlessFunction<T, R>>() {
			@Override
			public StandardInvocationHandlerBuilder<T> to(ParameterlessFunction<T, R> executed) {
				FunctionalInvocation<T> function = (T receiver, Object... arguments) -> executed.execute(receiver);
				return withHandling(method, function);
			}
		};
	}
	
	@Override
	public <R, A1> Binder<T, SingleParameterFunction<T, R, A1>> bind(SingleParameterFunction<? super T, R, A1> registered) {
		Method method = this.referenceExtractor.extractSingle(registered::execute);
		return new Binder<T, SingleParameterFunction<T, R, A1>>() {
			@Override
			public StandardInvocationHandlerBuilder<T> to(SingleParameterFunction<T, R, A1> executed) {
				@SuppressWarnings("unchecked")
				FunctionalInvocation<T> function =
						(T receiver, Object... arguments) -> executed.execute(receiver, (A1) arguments[0]);
				return withHandling(method, function);
			}
		};
	}
	
	private StandardInvocationHandlerBuilder<T> withHandling(Method invocable, FunctionalInvocation<T> function) {
		InvocationHandler<T> handler = invocation -> invocation.invokeAs(function);
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
		
		private final class MethodDispatchingDecomposer implements MethodBoundInvocation.Decomposer<T> {
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
			
			public Object dispatch(BoundInvocation<? extends T> invocation) throws Throwable {
				return this.handler.handle(invocation);
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
			final MethodDispatchingDecomposer decomposer = new MethodDispatchingDecomposer();
			methodInvocation.decompose(decomposer);
			return decomposer.dispatch(invocation);
		}
		
	}
	
}
