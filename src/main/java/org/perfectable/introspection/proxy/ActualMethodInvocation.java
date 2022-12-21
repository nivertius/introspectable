package org.perfectable.introspection.proxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import com.google.common.primitives.Primitives;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static org.perfectable.introspection.Introspections.introspect;

final class ActualMethodInvocation<T> implements MethodInvocation<T> {
	private static final Object[] EMPTY_ARGUMENTS = new Object[0];

	private final Method method;
	private final T receiver;
	private final @Nullable Object[] arguments;

	private transient @MonotonicNonNull MethodHandle handle;

	private static final MethodHandle PRIVATE_LOOKUP_CONSTRUCTOR = findPrivateLookupConstructor();

	public static <T> ActualMethodInvocation<T> intercepted(Method method,
													  T receiver, @Nullable Object @Nullable... arguments) {
		@Nullable Object[] actualArguments;
		if (arguments == null) {
			actualArguments = EMPTY_ARGUMENTS;
		}
		else {
			actualArguments = flattenVariableArguments(method, arguments);
		}
		return of(method, receiver, actualArguments);
	}

	public static <T> ActualMethodInvocation<T> of(Method method, T receiver, @Nullable Object... arguments) {
		verifyCallability(method, receiver, arguments);
		@Nullable Object[] argumentsClone = arguments.clone();
		return new ActualMethodInvocation<>(method, receiver, argumentsClone);
	}

	@SuppressWarnings("ArrayIsStoredDirectly")
	private ActualMethodInvocation(Method method, T receiver,
	                               @Nullable Object... arguments) {
		this.method = method;
		this.receiver = receiver;
		this.arguments = arguments;
	}

	@CanIgnoreReturnValue
	@Override
	@SuppressWarnings("IllegalCatch")
	public @Nullable Object invoke() throws Exception {
		createHandleIfNeeded();
		try {
			return handle.invoke();
		}
		catch (Exception | Error e) {
			throw e;
		}
		catch (Throwable e) {
			throw new AssertionError("Caught Throwable that is neither Exception or Error", e);
		}
	}

	@Override
	@CanIgnoreReturnValue
	public <R> R decompose(Decomposer<? super T, R> decomposer) {
		return decomposer.decompose(method, receiver, arguments);
	}


	@Override
	public MethodInvocation<T> withMethod(Method newMethod) {
		verifyReceiverCompatibility(newMethod, receiver);
		verifyArgumentsCompatibility(newMethod, arguments);
		return new ActualMethodInvocation<>(newMethod, receiver, arguments);
	}


	@Override
	public <X extends T> MethodInvocation<X> withReceiver(X newReceiver) {
		verifyReceiverCompatibility(method, newReceiver);
		return new ActualMethodInvocation<>(method, newReceiver, arguments);
	}

	@Override
	public MethodInvocation<T> withArguments(Object... newArguments) {
		verifyArgumentsCompatibility(method, newArguments);
		return new ActualMethodInvocation<>(method, receiver, newArguments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.method, System.identityHashCode(this.receiver), Arrays.hashCode(this.arguments));
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ActualMethodInvocation<?>)) {
			return false;
		}
		ActualMethodInvocation<?> other = (ActualMethodInvocation<?>) obj;
		return Objects.equals(this.method, other.method)
				&& Objects.equals(this.receiver, other.receiver)
				&& Arrays.equals(this.arguments, other.arguments);
	}

	private static void verifyCallability(Method method, @Nullable Object receiver, @Nullable Object... arguments) {
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

	private static void verifyArgumentsCompatibility(Method method, @Nullable Object... arguments) {
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
			@NonNull Class<?> parameterType;
			if (isVarArgs && i >= formals.length - 1) {
				@Nullable Class<?> componentType = formals[formals.length - 1].getComponentType();
				if (componentType == null) {
					throw new AssertionError("Method was variable arity, but its last parameter type has no component");
				}
				parameterType = (@NonNull Class<?>) componentType;
			}
			else {
				parameterType = formals[i];
			}
			@Nullable Object argument = arguments[i];
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

	private static @Nullable Object[] flattenVariableArguments(Method method, @Nullable Object[] actuals) {
		if (!method.isVarArgs()) {
			return actuals;
		}
		Class<?>[] formals = method.getParameterTypes();
		@SuppressWarnings("cast.unsafe")
		Object variableActual = (@NonNull Object) actuals[actuals.length - 1];
		int variableLength = Array.getLength(variableActual);
		int resultSize = (formals.length - 1) + variableLength;
		@Nullable Object[] result = new Object[resultSize];
		System.arraycopy(actuals, 0, result, 0, formals.length - 1);
		for (int i = 0; i < variableLength; i++) {
			result[formals.length - 1 + i] = Array.get(variableActual, i);
		}
		return result;
	}

	@SuppressWarnings("IllegalCatch")
	@EnsuresNonNull("handle")
	private void createHandleIfNeeded() {
		if (handle != null) {
			return;
		}
		MethodHandles.Lookup lookup;
		try {
			lookup = (MethodHandles.Lookup) PRIVATE_LOOKUP_CONSTRUCTOR.invoke(method.getDeclaringClass());
		}
		catch (Throwable throwable) {
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
		@SuppressWarnings("argument")
		MethodHandle createdHandle = MethodHandles.insertArguments(methodHandle, 0, arguments);
		this.handle = createdHandle;
	}

	@SuppressWarnings("MethodLength")
	private static MethodHandle findPrivateLookupConstructor() {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		Optional<Method> privateLookupInMethodOption = introspect(MethodHandles.class)
			.methods()
			.named("privateLookupIn")
			.parameters(Class.class, MethodHandles.Lookup.class)
			.returning(MethodHandles.Lookup.class)
			.asAccessible()
			.option();
		if (privateLookupInMethodOption.isPresent()) {
			Method privateLookupInMethod = privateLookupInMethodOption.get();
			MethodHandle methodHandle;
			try {
				methodHandle = lookup.unreflect(privateLookupInMethod);
			}
			catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
			return MethodHandles.insertArguments(methodHandle, 1, lookup);
		}
		Optional<Constructor<MethodHandles.Lookup>> noPreviousClassConstructorOption =
			introspect(MethodHandles.Lookup.class)
				.constructors()
				.parameters(Class.class, int.class)
				.asAccessible()
				.option();
		if (noPreviousClassConstructorOption.isPresent()) {
			Constructor<MethodHandles.Lookup> lookupConstructor = noPreviousClassConstructorOption.get();
			MethodHandle methodHandle;
			try {
				methodHandle = lookup.unreflectConstructor(lookupConstructor);
			}
			catch (IllegalAccessException e) {
				throw new AssertionError(e);
			}
			int allModifiers = MethodHandles.Lookup.PUBLIC | MethodHandles.Lookup.PROTECTED
				| MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PRIVATE;
			return MethodHandles.insertArguments(methodHandle, 1, allModifiers);
		}
		throw new AssertionError("Couldn't find constructor for Lookup, "
			+ "neither MethodHandles.Lookup(Class,int) (java<14) "
			+ "nor MethodHandles.privateLookupIn(Class, Lookup) is present (java>=9)");
	}

}
