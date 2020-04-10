package org.perfectable.introspection.proxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nullable;

import com.google.common.primitives.Primitives;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.perfectable.introspection.Introspections.introspect;

public final class MethodInvocation<T> implements Invocation {
	private static final Object[] EMPTY_ARGUMENTS = new Object[0];

	private final Method method;
	@Nullable
	private final T receiver;
	private final Object[] arguments;

	private transient MethodHandle handle;

	private static final MethodHandle PRIVATE_LOOKUP_CONSTRUCTOR;

	public static <T> MethodInvocation<T> intercepted(Method method,
													  @Nullable T receiver, @Nullable Object... arguments) {
		Object[] actualArguments = flattenVariableArguments(method, arguments);
		return of(method, receiver, actualArguments);
	}

	public static <T> MethodInvocation<T> of(Method method, @Nullable T receiver, Object... arguments) {
		requireNonNull(method);
		// receiver might be null
		requireNonNull(arguments);
		verifyCallability(method, receiver, arguments);
		Object[] argumentsClone = arguments.clone();
		return new MethodInvocation<>(method, receiver, argumentsClone);
	}

	private MethodInvocation(Method method, @Nullable T receiver,
							 Object... arguments) { // SUPPRESS ArrayIsStoredDirectly
		this.method = method;
		this.receiver = receiver;
		this.arguments = arguments;
	}

	@Nullable
	@Override
	public Object invoke() throws Throwable {
		if (handle == null) {
			handle = createHandle();
		}
		return handle.invoke();
	}

	@FunctionalInterface
	public interface Decomposer<T, R> {
		R decompose(Method method, @Nullable T receiver, Object... arguments);
	}

	public <R> R decompose(Decomposer<? super T, R> decomposer) {
		return decomposer.decompose(method, receiver, arguments.clone());
	}

	public MethodInvocation<T> withMethod(Method newMethod) {
		verifyReceiverCompatibility(newMethod, receiver);
		verifyArgumentsCompatibility(newMethod, arguments);
		return new MethodInvocation<>(newMethod, receiver, arguments);
	}

	public <X extends T> MethodInvocation<X> withReceiver(X newReceiver) {
		verifyReceiverCompatibility(method, newReceiver);
		return new MethodInvocation<>(method, newReceiver, arguments);
	}

	public MethodInvocation<T> withArguments(Object... newArguments) {
		verifyArgumentsCompatibility(method, newArguments);
		return new MethodInvocation<>(method, receiver, newArguments);
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
		if (!(obj instanceof MethodInvocation<?>)) {
			return false;
		}
		MethodInvocation<?> other = (MethodInvocation<?>) obj;
		return Objects.equals(this.method, other.method)
				&& Objects.equals(this.receiver, other.receiver)
				&& Arrays.equals(this.arguments, other.arguments);
	}

	private static void verifyCallability(Method method, @Nullable Object receiver, Object... arguments) {
		verifyReceiverCompatibility(method, receiver);
		verifyArgumentsCompatibility(method, arguments);
	}

	private static void verifyReceiverCompatibility(Method method, @Nullable Object receiver) {
		if ((method.getModifiers() & Modifier.STATIC) == 0) {
			checkArgument(receiver != null,
				"Method %s is not static, got null as receiver", method);
			Class<?> requiredType = method.getDeclaringClass();
			checkArgument(requiredType.isInstance(receiver),
				"Method %s requires %s as receiver, got %s", method, requiredType, receiver);
		}
		else {
			checkArgument(receiver == null,
				"Method %s is static, got %s as receiver", method, receiver);
		}
	}

	private static void verifyArgumentsCompatibility(Method method, Object... arguments) {
		Class<?>[] formals = method.getParameterTypes();
		boolean isVarArgs = method.isVarArgs();
		if (isVarArgs) {
			checkArgument(arguments.length >= formals.length - 1,
				"Method %s requires at least %s arguments, got %s", method, formals.length - 1, arguments.length);
		}
		else {
			checkArgument(arguments.length == formals.length,
				"Method %s requires %s arguments, got %s", method, formals.length, arguments.length);
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
				checkArgument(!parameterType.isPrimitive(),
					"Method %s has primitive %s as parameter %s, got null argument", method, parameterType, i + 1);
			}
			else {
				Class<?> argumentType = argument.getClass();
				Class<?> wrappedParameterType = Primitives.wrap(parameterType);
				checkArgument(wrappedParameterType.isAssignableFrom(argumentType),
					"Method %s takes %s as parameter %s, got %s as argument",
					method, wrappedParameterType, i + 1, argument);
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

	private MethodHandle createHandle() {
		MethodHandles.Lookup lookup;
		try {
			lookup = (MethodHandles.Lookup) PRIVATE_LOOKUP_CONSTRUCTOR.invoke(method.getDeclaringClass());
		}
		catch (Throwable throwable) { // SUPPRESS IllegalCatch will not fail
			throw new AssertionError(throwable);
		}
		MethodHandle methodHandle;
		try {
			methodHandle = lookup.unreflect(method);
		}
		catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
		if (receiver != null) {
			methodHandle = methodHandle.bindTo(receiver);
		}
		if (method.isVarArgs()) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			Class<?> lastParameterType = parameterTypes[parameterTypes.length - 1];
			int overflowArguments = arguments.length - parameterTypes.length + 1;
			methodHandle = methodHandle.asCollector(lastParameterType, overflowArguments);
		}
		return MethodHandles.insertArguments(methodHandle, 0, arguments);
	}

	static {
		Constructor<MethodHandles.Lookup> unique = introspect(MethodHandles.Lookup.class)
			.constructors()
			.parameters(Class.class, int.class)
			.asAccessible()
			.unique();
		MethodHandle methodHandle;
		try {
			methodHandle = MethodHandles.lookup()
				.unreflectConstructor(unique);
		}
		catch (IllegalAccessException e) {
			throw new AssertionError(e);
		}
		int allModifiers = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PROTECTED
			| MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PRIVATE;
		PRIVATE_LOOKUP_CONSTRUCTOR = MethodHandles.insertArguments(methodHandle, 1, allModifiers);
	}

}
