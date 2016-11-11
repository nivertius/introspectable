package org.perfectable.introspection.proxy;

@SuppressWarnings("FunctionalInterfaceClash")
public interface InvocationHandlerBuilder<T> {
	interface Binder<T, B> {
		InvocationHandlerBuilder<T> to(B executed);
	}
	
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
	
	Binder<T, ParameterlessProcedure<T>> bind(ParameterlessProcedure<? super T> registered);
	
	<R> Binder<T, ParameterlessFunction<T, R>> bind(ParameterlessFunction<? super T, R> registered);
	
	<A1> Binder<T, SingleParameterProcedure<T, A1>> bind(SingleParameterProcedure<? super T, A1> registered);
	
	<R, A1> Binder<T, SingleParameterFunction<T, R, A1>> bind(SingleParameterFunction<? super T, R, A1> registered);
	
	InvocationHandler<T> build(InvocationHandler<? super T> fallback);
	
	default InvocationHandler<T> build() {
		InvocationHandler<Object> throwingHandler = invocation -> {
			throw new UnsupportedOperationException("Unimplemented: " + invocation);
		};
		return build(throwingHandler);
	}
	
}
