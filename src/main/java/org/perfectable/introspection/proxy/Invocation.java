package org.perfectable.introspection.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Invocation<T> {
	private final Method method;
	private final T receiver;
	private final Object[] arguments;

	public static <T> Invocation<T> of(Method method, @Nullable T receiver, Object... arguments) {
		checkNotNull(method);
		// receiver might be null
		checkNotNull(arguments);
		verifyCallability(method, receiver, arguments);
		return new Invocation<>(method, receiver, arguments);
	}

	private Invocation(Method method, @Nullable T receiver, Object... arguments) {
		this.method = method;
		this.receiver = receiver;
		this.arguments = arguments;
	}

	// SUPPRESS NEXT IllegalThrows
	public Object invoke() throws Throwable {
		return proceed(Method::invoke);
	}

	// SUPPRESS NEXT IllegalThrows
	public Object proceed(Invoker<? super T> invoker) throws Throwable {
		try {
			return invoker.process(method, receiver, arguments);
		}
		catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	public <R> R decompose(Decomposer<? super T, R> decomposer) {
		return decomposer.decompose(method, receiver, arguments);
	}

	public interface Invoker<T> {
		// SUPPRESS NEXT IllegalThrows
		Object process(Method method, @Nullable T receiver, Object... arguments) throws Throwable;
	}

	public interface Decomposer<T, R> {
		R decompose(Method method, @Nullable T receiver, Object... arguments);
	}

	private static void verifyCallability(Method method, @Nullable Object receiver, Object... arguments) {
		if (receiver != null) {
			checkArgument(method.getDeclaringClass().isAssignableFrom(receiver.getClass()));
		}
		checkArgument(method.getParameterCount() == arguments.length);
		for (int i = 0; i < arguments.length; i++) {
			Class<?> parameterType = method.getParameterTypes()[i];
			Object argument = arguments[i];
			if (argument == null) {
				checkArgument(!parameterType.isPrimitive());
			}
			else {
				Class<?> argumentType = arguments[i].getClass();
				checkArgument(parameterType.isAssignableFrom(argumentType));
			}
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.method, this.receiver, Arrays.hashCode(this.arguments));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Invocation<?>)) {
			return false;
		}
		Invocation<?> other = (Invocation<?>) obj;
		return Objects.equals(this.method, other.method)
				&& Objects.equals(this.receiver, other.receiver)
				&& Arrays.equals(this.arguments, other.arguments);
	}
}