package com.googlecode.perfectable.introspection.proxy;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

public final class PreparedInvocable {
	
	private static final Object[] EMPTY_ARGUMENTS = new Object[0];
	
	private final Method method;
	private final Object[] arguments;
	
	public static PreparedInvocable of(Method method, @Nullable Object... arguments) {
		return new PreparedInvocable(method, arguments);
	}
	
	private PreparedInvocable(Method method, @Nullable Object... arguments) {
		this.method = method;
		this.arguments = arguments == null ? EMPTY_ARGUMENTS : arguments.clone();
	}
	
	public StaticInvocation asStatic() throws Throwable {
		return StaticInvocation.of(this.method, this.arguments);
	}
	
	public <T> BoundInvocation<T> bind(T receiver) {
		checkNotNull(receiver);
		return BoundInvocation.of(this.method, receiver, this.arguments);
	}
	
	public void decompose(Invocation.Decomposer decomposer) {
		DecompositionHelper.start(decomposer)
				.method(this.method)
				.arguments(this.arguments);
	}
	
	@SuppressWarnings("unchecked")
	public Invocable stripArguments() {
		Invocable safeInvocable = Invocable.of(this.method);
		return safeInvocable;
	}
	
}
