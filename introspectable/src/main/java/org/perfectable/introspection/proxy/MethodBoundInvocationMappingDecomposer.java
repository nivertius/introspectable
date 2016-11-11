package org.perfectable.introspection.proxy;

import java.lang.reflect.Method;
import java.util.function.Function;
import javax.annotation.Nullable;

final class MethodBoundInvocationMappingDecomposer<T> implements
		MethodBoundInvocation.Decomposer<BoundInvocation<? extends T>, T> {
	
	public static <T> MethodBoundInvocationMappingDecomposer<T> identity() {
		@SuppressWarnings("unchecked")
		Function<Method, Invocable<T>> methodTransformer = method -> (Invocable<T>) MethodInvocable.of(method);
		Function<T, T> receiverTransformer = Function.identity();
		ArgumentTransformer argumentTransformer = new ArgumentTransformer() {
			@Override
			public <X> X transform(int index, Class<X> formal, X actual) {
				return actual;
			}
		};
		return new MethodBoundInvocationMappingDecomposer<>(methodTransformer, receiverTransformer, argumentTransformer);
	}
	
	public MethodBoundInvocationMappingDecomposer<T> withMethodTransformer(
			Function<Method, Invocable<T>> newMethodTransformer) {
		return new MethodBoundInvocationMappingDecomposer<>(newMethodTransformer, this.receiverTransformer,
				this.argumentTransformer);
	}
	
	public MethodBoundInvocationMappingDecomposer<T> withReceiverTransformer(
			Function<T, ? extends T> newReceiverTransformer) {
		return new MethodBoundInvocationMappingDecomposer<>(this.methodTransformer, newReceiverTransformer,
				this.argumentTransformer);
	}
	
	private MethodBoundInvocationMappingDecomposer(Function<Method, Invocable<T>> methodTransformer,
			Function<T, ? extends T> receiverTransformer, ArgumentTransformer argumentTransformer) {
		this.methodTransformer = methodTransformer;
		this.receiverTransformer = receiverTransformer;
		this.argumentTransformer = argumentTransformer;
	}
	
	public interface ArgumentTransformer {
		<X> X transform(int index, Class<X> formal, X actual);
	}
	
	private final Function<Method, Invocable<T>> methodTransformer;
	private final Function<T, ? extends T> receiverTransformer;
	private final ArgumentTransformer argumentTransformer;
	
	@Nullable
	private Invocable<T> invocable;
	@Nullable
	private BoundInvocable<T> boundInvocable;
	@Nullable
	private Object[] arguments;
	
	@Override
	public void method(Method method) {
		this.arguments = new Object[method.getParameterCount()];
		this.invocable = this.methodTransformer.apply(method);
	}
	
	@Override
	public void receiver(T receiver) {
		T newReceiver = this.receiverTransformer.apply(receiver);
		this.boundInvocable = this.invocable.bind(newReceiver);
	}
	
	@Override
	public <X> void argument(int index, Class<? super X> formal, X actual) {
		this.arguments[index] = this.argumentTransformer.transform(index, formal, actual);
	}
	
	@Override
	public BoundInvocation<? extends T> finish() {
		return this.boundInvocable.prepare(this.arguments);
	}
	
}
