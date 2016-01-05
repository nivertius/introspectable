package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

public final class MethodInvocable<T> implements Invocable<T> {
	
	public static final MethodInvocable<Object> OBJECT_EQUALS = extractObjectMethod("equals", Object.class);
	public static final MethodInvocable<Object> OBJECT_TO_STRING = extractObjectMethod("toString");
	
	private final Method method;
	
	public static MethodInvocable<?> of(Method method) {
		return new MethodInvocable<>(method);
	}
	
	private MethodInvocable(Method method) {
		this.method = method;
	}
	
	@Override
	public Object invoke(T receiver, Object... arguments) throws Throwable {
		try {
			return this.method.invoke(receiver, arguments);
		}
		catch(InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public MethodPreparedInvocable<T> prepare(Object... arguments) {
		return (MethodPreparedInvocable<T>) MethodPreparedInvocable.of(this.method, arguments);
	}
	
	@Override
	public MethodBoundInvocable<T> bind(T receiver) {
		return MethodBoundInvocable.of(this.method, receiver);
	}
	
	public interface Decomposer<R> {
		void method(Method method);
		
		R finish();
	}
	
	public <R> R decompose(Decomposer<R> decomposer) {
		decomposer.method(this.method);
		return decomposer.finish();
	}
	
	public boolean isDeclaredBy(Class<?> candidateDeclaringClass) {
		return this.method.getDeclaringClass().equals(candidateDeclaringClass);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.method);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(!(obj instanceof MethodInvocable<?>)) {
			return false;
		}
		MethodInvocable<?> other = (MethodInvocable<?>) obj;
		return Objects.equals(this.method, other.method);
	}
	
	@Override
	public String toString() {
		return "Invocable.of(" + this.method.toString() + ")";
	}
	
	private static MethodInvocable<Object> extractObjectMethod(String name, Class<?>... parameterTypes) {
		Method method;
		try {
			method = Object.class.getDeclaredMethod(name, parameterTypes);
		}
		catch(NoSuchMethodException | SecurityException e) {
			throw new AssertionError("Object is missing standard method", e);
		}
		@SuppressWarnings("unchecked")
		MethodInvocable<Object> casted = (MethodInvocable<Object>) MethodInvocable.of(method);
		return casted;
	}
	
}
