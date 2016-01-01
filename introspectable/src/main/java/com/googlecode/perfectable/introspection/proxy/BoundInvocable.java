package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.Method;
import java.util.Objects;

public final class BoundInvocable<T> {
	private final Method method;
	private final T receiver;
	
	public static <T> BoundInvocable<T> of(Method method, T receiver) {
		return new BoundInvocable<>(method, receiver);
	}
	
	private BoundInvocable(Method method, T receiver) {
		this.method = method;
		this.receiver = receiver;
	}
	
	public BoundInvocation<T> prepare(Object... arguments) {
		return BoundInvocation.of(this.method, this.receiver, arguments);
	}
	
	public Invocable stripReceiver() {
		return Invocable.of(this.method);
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
		if(!(obj instanceof BoundInvocable<?>)) {
			return false;
		}
		BoundInvocable<?> other = (BoundInvocable<?>) obj;
		return Objects.equals(this.method, other.method) &&
				Objects.equals(this.receiver, other.receiver);
	}
}
