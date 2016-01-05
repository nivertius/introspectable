package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class MethodBoundInvocable<T> implements BoundInvocable<T> {
	private final Method method;
	private final T receiver;
	
	public static <T> MethodBoundInvocable<T> of(Method method, T receiver) {
		return new MethodBoundInvocable<>(method, receiver);
	}
	
	private MethodBoundInvocable(Method method, T receiver) {
		this.method = method;
		this.receiver = receiver;
	}
	
	@Override
	public Object invoke(Object... arguments) throws Throwable {
		try {
			return this.method.invoke(this.receiver, arguments);
		}
		catch(InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
	@Override
	public MethodBoundInvocation<T> prepare(Object... arguments) {
		return MethodBoundInvocation.of(this.method, this.receiver, arguments);
	}
	
	@SuppressWarnings("unchecked")
	public MethodInvocable<T> stripReceiver() {
		return (MethodInvocable<T>) MethodInvocable.of(this.method);
	}
	
	public interface Decomposer<R, T> {
		void method(Method method);
		
		void receiver(T receiver);
		
		R finish();
	}
	
	public <R> R decompose(Decomposer<R, ? super T> decomposer) {
		decomposer.method(this.method);
		decomposer.receiver(this.receiver);
		return decomposer.finish();
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.method, this.receiver);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(!(obj instanceof MethodBoundInvocable<?>)) {
			return false;
		}
		MethodBoundInvocable<?> other = (MethodBoundInvocable<?>) obj;
		return Objects.equals(this.method, other.method) &&
				Objects.equals(this.receiver, other.receiver);
	}
}
