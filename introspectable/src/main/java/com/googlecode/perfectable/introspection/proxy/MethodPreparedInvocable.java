package com.googlecode.perfectable.introspection.proxy;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

public final class MethodPreparedInvocable implements PreparedInvocable {
	
	private static final Object[] EMPTY_ARGUMENTS = new Object[0];
	
	private final Method method;
	private final Object[] arguments;
	
	public static MethodPreparedInvocable of(Method method, @Nullable Object... arguments) {
		return new MethodPreparedInvocable(method, arguments);
	}
	
	private MethodPreparedInvocable(Method method, @Nullable Object... arguments) {
		this.method = method;
		this.arguments = arguments == null ? EMPTY_ARGUMENTS : arguments.clone();
	}
	
	@Override
	public MethodStaticInvocation asStatic() {
		return MethodStaticInvocation.of(this.method, this.arguments);
	}
	
	@Override
	public <T> MethodBoundInvocation<T> bind(T receiver) {
		checkNotNull(receiver);
		return MethodBoundInvocation.of(this.method, receiver, this.arguments);
	}
	
	public interface Decomposer {
		void method(Method method);
		
		<T> void argument(int index, Class<? super T> formal, T actual);
	}
	
	public void decompose(Decomposer decomposer) {
		decomposer.method(this.method);
		DecompositionHelper.decomposeArguments(this.method, this.arguments, decomposer::argument);
	}
	
	@Override
	public Invocable stripArguments() {
		Invocable safeInvocable = MethodInvocable.of(this.method);
		return safeInvocable;
	}
	
}
