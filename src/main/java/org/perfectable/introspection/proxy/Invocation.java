package org.perfectable.introspection.proxy;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nullable;

import com.google.common.primitives.Primitives;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public final class Invocation<T> {
	private static final Object[] EMPTY_ARGUMENTS = new Object[0];

	private final Method method;
	private final T receiver;
	private final Object[] arguments;

	public static <T> Invocation<T> intercepted(Method method, @Nullable T receiver, @Nullable Object... arguments) {
		Object[] actualArguments = flattenVariableArguments(method, arguments);
		return of(method, receiver, actualArguments);
	}

	public static <T> Invocation<T> of(Method method, @Nullable T receiver, Object... arguments) {
		requireNonNull(method);
		// receiver might be null
		requireNonNull(arguments);
		verifyCallability(method, receiver, arguments);
		return new Invocation<>(method, receiver, arguments);
	}

	private Invocation(Method method, @Nullable T receiver, Object... arguments) {
		this.method = method;
		this.receiver = receiver;
		this.arguments = arguments;
	}

	@Nullable
	public Object invoke() throws Throwable { // SUPPRESS IllegalThrows
		return proceed(MethodInvoker.INSTANCE);
	}

	@Nullable
	public Object proceed(Invoker<? super T> invoker) throws Throwable { // SUPPRESS IllegalThrows
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

	public <X extends T> Invocation<X> withReceiver(X newReceiver) {
		return of(method, newReceiver, arguments);
	}

	@FunctionalInterface
	public interface Invoker<T> {
		// SUPPRESS NEXT 2 IllegalThrows
		@Nullable
		Object process(Method method, @Nullable T receiver, Object... arguments) throws Throwable;
	}

	@FunctionalInterface
	public interface Decomposer<T, R> {
		R decompose(Method method, @Nullable T receiver, Object... arguments);
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

	private static final class MethodInvoker implements Invoker<Object> {
		public static final MethodInvoker INSTANCE = new MethodInvoker();

		private MethodInvoker() {
			// singleton
		}

		@Nullable
		@Override
		public Object process(Method method, @Nullable Object receiver, Object... arguments) throws Throwable {
			Object[] actualArguments = composeVariableArguments(method, arguments);
			return method.invoke(receiver, actualArguments);
		}
	}

	private static void verifyCallability(Method method, @Nullable Object receiver, Object... arguments) {
		if (receiver != null) {
			checkArgument(method.getDeclaringClass().isAssignableFrom(receiver.getClass()));
		}
		Class<?>[] formals = method.getParameterTypes();
		boolean isVarArgs = method.isVarArgs();
		if (isVarArgs) {
			checkArgument(arguments.length >= formals.length - 1);
		}
		else {
			checkArgument(arguments.length == formals.length);
		}
		for (int i = 0; i < arguments.length; i++) {
			Class<?> parameterType;
			if (isVarArgs && i >= formals.length - 1) {
				parameterType = formals[formals.length - 1].getComponentType();
			}
			else {
				parameterType = formals[i];
			}
			Object argument = arguments[i];
			if (argument == null) {
				checkArgument(!parameterType.isPrimitive());
			}
			else {
				Class<?> argumentType = argument.getClass();
				Class<?> wrappedParameterType = Primitives.wrap(parameterType);
				checkArgument(wrappedParameterType.isAssignableFrom(argumentType));
			}
		}
	}

	private static Object[] flattenVariableArguments(Method method, @Nullable Object[] actuals) { // SUPPRESS UseVarargs
		if (actuals == null) {
			return EMPTY_ARGUMENTS;
		}
		if (!method.isVarArgs()) {
			return actuals;
		}
		Class<?>[] formals = method.getParameterTypes();
		Object variableActual = actuals[actuals.length - 1];
		int variableLength = Array.getLength(variableActual);
		int resultSize = (formals.length - 1) + variableLength;
		Object[] result = new Object[resultSize];
		System.arraycopy(actuals, 0, result, 0, formals.length - 1);
		for (int i = 0; i < variableLength; i++) {
			result[formals.length - 1 + i] = Array.get(variableActual, i);
		}
		return result;
	}

	private static Object[] composeVariableArguments(Method method, Object[] provided) { // SUPPRESS UseVarargs
		if (!method.isVarArgs()) {
			return provided;
		}
		Class<?>[] formals = method.getParameterTypes();
		Class<?> variableFormal = formals[formals.length - 1].getComponentType();
		int variableLength = provided.length - (formals.length - 1);
		int resultSize = formals.length;
		Object[] result = new Object[resultSize];
		System.arraycopy(provided, 0, result, 0, formals.length - 1);
		Object variableActual = Array.newInstance(variableFormal, variableLength);
		for (int i = 0; i < variableLength; i++) {
			Object value = provided[(formals.length - 1) + i];
			Array.set(variableActual, i, value);
		}
		result[formals.length - 1] = variableActual;
		return result;
	}
}
