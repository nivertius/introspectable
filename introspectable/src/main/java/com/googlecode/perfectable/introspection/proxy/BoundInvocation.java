package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class BoundInvocation<T> implements Invocation {
	private final Method method;
	private final T receiver;
	private final Object[] arguments;
	
	public static <T> BoundInvocation<T> of(Method method, T receiver, Object... arguments) {
		return new BoundInvocation<>(method, receiver, arguments);
	}
	
	public BoundInvocation(Method method, T receiver, Object... arguments) {
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
	public void decompose(Decomposer decomposer) {
		DecompositionHelper.start(decomposer)
				.method(this.method)
				.receiver(this.receiver)
				.arguments(this.arguments);
	}
	
	public BoundInvocation<T> withReceiver(T newReceiver) {
		return of(this.method, newReceiver, this.arguments);
	}
	
	public PreparedInvocable stripReceiver() {
		return PreparedInvocable.of(this.method, this.arguments);
	}
	
	public BoundInvocable<T> stripArguments() {
		return BoundInvocable.of(this.method, this.receiver);
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
		if(!(obj instanceof BoundInvocation<?>)) {
			return false;
		}
		BoundInvocation<?> other = (BoundInvocation<?>) obj;
		return Objects.equals(this.method, other.method) &&
				Objects.equals(this.receiver, other.receiver) &&
				Arrays.equals(this.arguments, other.arguments);
	}
	
}