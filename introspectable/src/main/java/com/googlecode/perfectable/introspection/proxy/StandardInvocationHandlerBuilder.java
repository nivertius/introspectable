package com.googlecode.perfectable.introspection.proxy;

import com.google.common.collect.ImmutableMap;
import com.googlecode.perfectable.introspection.ReferenceExtractor;
import com.googlecode.perfectable.introspection.proxy.BoundInvocation.FunctionalInvocation;

public class StandardInvocationHandlerBuilder<T> implements InvocationHandlerBuilder<T> {
	private final ReferenceExtractor<T> referenceExtractor;
	private final ImmutableMap<Invocable, InvocationHandler<T>> methods;
	
	static <T> StandardInvocationHandlerBuilder<T> start(Class<T> sourceClass) {
		ReferenceExtractor<T> referenceExtractor = ReferenceExtractor.of(sourceClass);
		ImmutableMap<Invocable, InvocationHandler<T>> methods = ImmutableMap.of();
		return new StandardInvocationHandlerBuilder<>(referenceExtractor, methods);
	}
	
	private StandardInvocationHandlerBuilder(ReferenceExtractor<T> referenceExtractor,
			ImmutableMap<Invocable, InvocationHandler<T>> methods) {
		this.referenceExtractor = referenceExtractor;
		this.methods = methods;
	}
	
	@Override
	public Binder<T, ParameterlessProcedure<T>> bind(ParameterlessProcedure<? super T> registered) {
		Invocable invocable = this.referenceExtractor.extractNone(registered::execute);
		return new Binder<T, ParameterlessProcedure<T>>() {
			@Override
			public StandardInvocationHandlerBuilder<T> to(ParameterlessProcedure<T> executed) {
				FunctionalInvocation<T> function = (T receiver, Object... arguments) -> {
					executed.execute(receiver);
					return null;
				};
				return withHandling(invocable, function);
			}
		};
	}
	
	@Override
	public <A1> Binder<T, SingleParameterProcedure<T, A1>> bind(SingleParameterProcedure<? super T, A1> registered) {
		Invocable invocable = this.referenceExtractor.extractSingle(registered::execute);
		return new Binder<T, SingleParameterProcedure<T, A1>>() {
			@Override
			public StandardInvocationHandlerBuilder<T> to(SingleParameterProcedure<T, A1> executed) {
				@SuppressWarnings("unchecked")
				FunctionalInvocation<T> function = (T receiver, Object... arguments) -> {
					executed.execute(receiver, (A1) arguments[0]);
					return null;
				};
				return withHandling(invocable, function);
			}
		};
	}
	
	@Override
	public <R> Binder<T, ParameterlessFunction<T, R>> bind(ParameterlessFunction<? super T, R> registered) {
		Invocable invocable = this.referenceExtractor.extractNone(registered::execute);
		return new Binder<T, ParameterlessFunction<T, R>>() {
			@Override
			public StandardInvocationHandlerBuilder<T> to(ParameterlessFunction<T, R> executed) {
				FunctionalInvocation<T> function = (T receiver, Object... arguments) -> executed.execute(receiver);
				return withHandling(invocable, function);
			}
		};
	}
	
	@Override
	public <R, A1> Binder<T, SingleParameterFunction<T, R, A1>> bind(SingleParameterFunction<? super T, R, A1> registered) {
		Invocable invocable = this.referenceExtractor.extractSingle(registered::execute);
		return new Binder<T, SingleParameterFunction<T, R, A1>>() {
			@Override
			public StandardInvocationHandlerBuilder<T> to(SingleParameterFunction<T, R, A1> executed) {
				@SuppressWarnings("unchecked")
				FunctionalInvocation<T> function =
						(T receiver, Object... arguments) -> executed.execute(receiver, (A1) arguments[0]);
				return withHandling(invocable, function);
			}
		};
	}
	
	private StandardInvocationHandlerBuilder<T> withHandling(Invocable invocable, FunctionalInvocation<T> function) {
		InvocationHandler<T> handler = invocation -> invocation.invokeAs(function);
		ImmutableMap<Invocable, InvocationHandler<T>> newMethods =
				ImmutableMap.<Invocable, InvocationHandler<T>> builder().putAll(this.methods).put(invocable, handler)
						.build();
		return new StandardInvocationHandlerBuilder<>(this.referenceExtractor, newMethods);
	}
	
	@Override
	public InvocationHandler<T> build(InvocationHandler<? super T> fallback) {
		return new DispatchingInvocationHandler<>(this.methods, fallback);
	}
	
	private static final class DispatchingInvocationHandler<T> implements InvocationHandler<T> {
		
		private final InvocationHandler<? super T> fallback;
		private final ImmutableMap<Invocable, InvocationHandler<T>> methods;
		
		DispatchingInvocationHandler(ImmutableMap<Invocable, InvocationHandler<T>> methods,
				InvocationHandler<? super T> fallback) {
			this.methods = methods;
			this.fallback = fallback;
		}
		
		@Override
		public Object handle(BoundInvocation<? extends T> invocation) throws Throwable {
			Invocable invocable = invocation.stripReceiver().stripArguments();
			InvocationHandler<T> handler = this.methods.get(invocable);
			if(handler != null) {
				return handler.handle(invocation);
			}
			return this.fallback.handle(invocation);
		}
		
	}
	
}
