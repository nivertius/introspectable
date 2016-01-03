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
	public StandardInvocationHandlerBuilder<T> withHandling(ParameterlessProcedure<? super T> registered,
			ParameterlessProcedure<T> handled) {
		Invocable invocable = this.referenceExtractor.extractNone(registered::execute);
		FunctionalInvocation<T> function = (T receiver, Object... arguments) -> {
			handled.execute(receiver);
			return null;
		};
		return withHandling(invocable, function);
	}
	
	@Override
	public <A1> StandardInvocationHandlerBuilder<T> withHandling(SingleParameterProcedure<? super T, A1> registered,
			SingleParameterProcedure<T, A1> handled) {
		Invocable invocable = this.referenceExtractor.extractSingle(registered::execute);
		@SuppressWarnings("unchecked")
		FunctionalInvocation<T> function = (T receiver, Object... arguments) -> {
			handled.execute(receiver, (A1) arguments[0]);
			return null;
		};
		return withHandling(invocable, function);
	}
	
	@Override
	public <R> StandardInvocationHandlerBuilder<T> withHandling(ParameterlessFunction<? super T, R> registered,
			ParameterlessFunction<T, R> handled) {
		Invocable invocable = this.referenceExtractor.extractNone(registered::execute);
		FunctionalInvocation<T> function = (T receiver, Object... arguments) -> {
			return handled.execute(receiver);
		};
		return withHandling(invocable, function);
	}
	
	@Override
	public <R, A1> StandardInvocationHandlerBuilder<T> withHandling(SingleParameterFunction<? super T, R, A1> registered,
			SingleParameterFunction<T, R, A1> handled) {
		Invocable invocable = this.referenceExtractor.extractSingle(registered::execute);
		@SuppressWarnings("unchecked")
		FunctionalInvocation<T> function = (T receiver, Object... arguments) -> {
			return handled.execute(receiver, (A1) arguments[0]);
		};
		return withHandling(invocable, function);
	}
	
	private StandardInvocationHandlerBuilder<T> withHandling(Invocable invocable, FunctionalInvocation<T> function) {
		InvocationHandler<T> handler = invocation -> invocation.invokeAs(function);
		ImmutableMap<Invocable, InvocationHandler<T>> newMethods =
				ImmutableMap.<Invocable, InvocationHandler<T>> builder().putAll(this.methods).put(invocable, handler)
						.build();
		return new StandardInvocationHandlerBuilder<>(this.referenceExtractor, newMethods);
	}
	
	@Override
	public InvocationHandler<T> build() {
		return new DispatchingInvocationHandler<>(this.methods);
	}
	
	private static final class DispatchingInvocationHandler<T> implements InvocationHandler<T> {
		
		private final ImmutableMap<Invocable, InvocationHandler<T>> methods;
		
		DispatchingInvocationHandler(ImmutableMap<Invocable, InvocationHandler<T>> methods) {
			this.methods = methods;
		}
		
		@Override
		public Object handle(BoundInvocation<T> invocation) throws Throwable {
			Invocable invocable = invocation.stripReceiver().stripArguments();
			InvocationHandler<T> handler = this.methods.get(invocable);
			if(handler == null) {
				throw new UnsupportedOperationException("Invocable " + invocable + " is not supported");
			}
			return handler.handle(invocation);
		}
		
	}
	
}
