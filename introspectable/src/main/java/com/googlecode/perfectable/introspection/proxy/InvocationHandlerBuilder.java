package com.googlecode.perfectable.introspection.proxy;

public interface InvocationHandlerBuilder<T> {
	@FunctionalInterface
	interface ParameterlessProcedure<T> {
		void execute(T self);
	}
	
	@FunctionalInterface
	interface SingleParameterProcedure<T, A1> {
		void execute(T self, A1 argument1);
	}
	
	@FunctionalInterface
	interface ParameterlessFunction<T, R> {
		R execute(T self);
	}
	
	@FunctionalInterface
	interface SingleParameterFunction<T, R, A1> {
		R execute(T self, A1 argument);
	}
	
	InvocationHandlerBuilder<T> withHandling(ParameterlessProcedure<? super T> registered,
			ParameterlessProcedure<T> handled);
	
	<R> InvocationHandlerBuilder<T> withHandling(ParameterlessFunction<? super T, R> registered,
			ParameterlessFunction<T, R> handled);
	
	<A1> InvocationHandlerBuilder<T> withHandling(SingleParameterProcedure<? super T, A1> registered,
			SingleParameterProcedure<T, A1> handled);
	
	<R, A1> InvocationHandlerBuilder<T> withHandling(SingleParameterFunction<? super T, R, A1> registered,
			SingleParameterFunction<T, R, A1> handled);
	
	InvocationHandler<T> build();
	
}
