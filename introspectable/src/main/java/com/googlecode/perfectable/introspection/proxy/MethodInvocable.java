package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.Method;
import java.util.Objects;

public final class MethodInvocable implements Invocable {
	
	private final Method method;
	
	public static MethodInvocable of(Method method) {
		return new MethodInvocable(method);
	}
	
	private MethodInvocable(Method method) {
		this.method = method;
	}
	
	@Override
	public PreparedInvocable prepare(Object... arguments) {
		return MethodPreparedInvocable.of(this.method, arguments);
	}
	
	@Override
	public <T> MethodBoundInvocable<T> bind(T receiver) {
		return MethodBoundInvocable.of(this.method, receiver);
	}
	
	@Override
	public void decompose(Invocation.Decomposer decomposer) {
		DecompositionHelper.start(decomposer)
				.method(this.method);
	}
	
	@Override
	public Class<?> expectedResultType() {
		return this.method.getReturnType();
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
		if(!(obj instanceof MethodInvocable)) {
			return false;
		}
		MethodInvocable other = (MethodInvocable) obj;
		return Objects.equals(this.method, other.method);
	}
	
	@Override
	public String toString() {
		return "Invocable.of(" + this.method.toString() + ")";
	}
}
