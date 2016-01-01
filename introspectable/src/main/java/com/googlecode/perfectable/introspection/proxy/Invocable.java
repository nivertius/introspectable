package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.Method;
import java.util.Objects;

public final class Invocable {
	private final Method method;
	
	public static Invocable of(Method method) {
		return new Invocable(method);
	}
	
	private Invocable(Method method) {
		this.method = method;
	}
	
	public PreparedInvocable prepare(Object... arguments) {
		return PreparedInvocable.of(this.method, arguments);
	}
	
	public <T> BoundInvocable<T> bind(T receiver) {
		return BoundInvocable.of(this.method, receiver);
	}
	
	public void decompose(Invocation.Decomposer decomposer) {
		DecompositionHelper.start(decomposer)
				.method(this.method);
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
		if(!(obj instanceof Invocable)) {
			return false;
		}
		Invocable other = (Invocable) obj;
		return Objects.equals(this.method, other.method);
	}
	
}
