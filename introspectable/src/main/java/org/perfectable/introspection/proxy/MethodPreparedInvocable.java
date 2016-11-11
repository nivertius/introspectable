package org.perfectable.introspection.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

public final class MethodPreparedInvocable<T> implements PreparedInvocable<T> {

	private static final Object[] EMPTY_ARGUMENTS = new Object[0];

	private final Method method;
	private final Object[] arguments;

	public static MethodPreparedInvocable<?> of(Method method, @Nullable Object... arguments) {
		return new MethodPreparedInvocable<>(method, arguments);
	}

	private MethodPreparedInvocable(Method method, @Nullable Object... arguments) {
		this.method = method;
		this.arguments = arguments == null ? EMPTY_ARGUMENTS : arguments.clone();
	}

	@Override
	public Object invoke(T receiver) throws Throwable {
		try {
			return this.method.invoke(receiver, this.arguments);
		}
		catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	@Override
	public MethodStaticInvocation asStatic() {
		return MethodStaticInvocation.of(this.method, this.arguments);
	}

	@Override
	public MethodBoundInvocation<T> bind(T receiver) {
		checkNotNull(receiver);
		return MethodBoundInvocation.of(this.method, receiver, this.arguments);
	}

	public interface Decomposer<R> {
		void method(Method method);

		<T> void argument(int index, Class<? super T> formal, T actual);

		R finish();
	}

	public <R> R decompose(Decomposer<R> decomposer) {
		decomposer.method(this.method);
		DecompositionHelper.decomposeArguments(this.method, this.arguments, decomposer::argument);
		return decomposer.finish();
	}

	@SuppressWarnings("unchecked")
	public MethodInvocable<T> stripArguments() {
		MethodInvocable<T> safeInvocable = (MethodInvocable<T>) MethodInvocable.of(this.method);
		return safeInvocable;
	}

}
