package com.googlecode.perfectable.introspection.proxy;

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
	public MethodBoundInvocation<T> prepare(Object... arguments) {
		return MethodBoundInvocation.of(this.method, this.receiver, arguments);
	}
	
	@Override
	public MethodInvocable stripReceiver() {
		return MethodInvocable.of(this.method);
	}
	
	public void decompose(Invocation.Decomposer decomposer) {
		DecompositionHelper.start(decomposer)
				.method(this.method)
				.receiver(this.receiver);
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
