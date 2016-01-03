package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class MethodBoundInvocation<T> implements BoundInvocation<T> {
	private final Method method;
	private final T receiver;
	private final Object[] arguments;
	
	public static <T> MethodBoundInvocation<T> of(Method method, T receiver, Object... arguments) {
		return new MethodBoundInvocation<>(method, receiver, arguments);
	}
	
	public MethodBoundInvocation(Method method, T receiver, Object... arguments) {
		this.method = method;
		this.receiver = receiver;
		this.arguments = arguments;
	}
	
	@Override
	public Object invoke() throws Throwable {
		try {
			Object result = this.method.invoke(this.receiver, this.arguments);
			return result;
		}
		catch(InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
	@Override
	public Object invokeAs(FunctionalInvocation<T> function)
			throws Throwable {
		return function.invoke(this.receiver, this.arguments);
	}
	
	public void decompose(Decomposer decomposer) {
		DecompositionHelper.start(decomposer)
				.method(this.method)
				.receiver(this.receiver)
				.arguments(this.arguments);
	}
	
	@Override
	public MethodBoundInvocation<T> withReceiver(T newReceiver) {
		return of(this.method, newReceiver, this.arguments);
	}
	
	@Override
	public MethodPreparedInvocable stripReceiver() {
		return MethodPreparedInvocable.of(this.method, this.arguments);
	}
	
	@Override
	public MethodBoundInvocable<T> stripArguments() {
		return MethodBoundInvocable.of(this.method, this.receiver);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.method, this.receiver, this.arguments);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(!(obj instanceof MethodBoundInvocation<?>)) {
			return false;
		}
		MethodBoundInvocation<?> other = (MethodBoundInvocation<?>) obj;
		return Objects.equals(this.method, other.method) &&
				Objects.equals(this.receiver, other.receiver) &&
				Arrays.equals(this.arguments, other.arguments);
	}
	
}
