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
import javax.annotation.Nullable;

import com.google.common.primitives.Primitives;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.concurrent.LazyInit;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.perfectable.introspection.Introspections.introspect;

/**
 * Capture of a information needed to invoke a method.
 *
 * <p>This class collects and mimics arguments received by different proxy frameworks when method is intercepted on
 * proxy object, represents it in uniform fashion and allows some manipulations. Instances of this class can be also
 * constructed synthetically, as if the call was made but intercepted before execution.
 *
 * <p>This class handles three elements of a call: Method that was called, a receiver, that is object on which method
 * was called, and arguments that were passed with the call. Receiver can be omitted, if the method is static.
 *
 * <p>There are two most important methods on this class: {@link #invoke} which will execute the method with specified
 * arguments and receiver, and return result or throw an exception. The other method is {@link #decompose} which
 * allows introspection of structure of this invocation.
 *
 * <p>Objects of this class are unmodifiable, that is methods of this class that would change the object actually
 * produce new changed object. It is not immutable, because both receiver and arguments passed can, and often are,
 * mutable.
 *
 * @param <T> Type of method receiver (i.e. {@code this})
 */
public final class MethodInvocation<T> implements Invocation {
	private static final Object[] EMPTY_ARGUMENTS = new Object[0];

	private final Method method;
	@Nullable
	private final T receiver;
	private final Object[] arguments;

	@LazyInit
	private transient MethodHandle handle;

	private static final MethodHandle PRIVATE_LOOKUP_CONSTRUCTOR = findPrivateLookupConstructor();

	/**
	 * Create invocation that was intercepted from proxy mechanism.
	 *
	 * <p>This method assumes that if {@code method} is vararg, and requires X non-vararg parameters, the
	 * {@code arguments} passed here contains exactly X+1 elements, where X one are the non-vararg arguments that are
	 * compatible with non-vararg parameter type, and the last element is an array of elements of the vararg type.
	 * This is how the method call is represented in runtime for varargs. This method will 'unroll' the last array
	 * argument and create invocation that has <i>flat</i> arguments array.
	 *
	 * <p>For non-varargs {@code method}, this method is identical to {@link #of}, i.e. {@code arguments} must be a
	 * <i>flat</i> array with element count exactly equal to method parameter count, and with compatible types.
	 *
	 * @param method method that was/will be called on invocation
	 * @param receiver receiver of the method call (i.e. {@code this})
	 * @param arguments arguments in a runtime representation
	 * @param <T> type of receiver
	 * @return method invocation comprised from passed arguments
	 * @throws IllegalArgumentException when method invocation is illegal and will not succeed: ex. method is static
	 *     and receiver was provided (or other way around), receiver is of wrong type for the provided method,
	 *     or arguments are not matching method parameter types.
	 */
	public static <T> MethodInvocation<T> intercepted(Method method,
													  @Nullable T receiver, @Nullable Object... arguments) {
		Object[] actualArguments = flattenVariableArguments(method, arguments);
		return of(method, receiver, actualArguments);
	}

	/**
	 * Create synthetic invocation from scratch.
	 *
	 * <p>This method assumes that if {@code method} is vararg, and requires X non-vararg parameters,
	 * {@code arguments} contain at least X elements, where each of these elements is compatible with corresponding
	 * parameter of the method, and any amount of elements that are compatible with the variable parameter of the
	 * method.
	 *
	 * <p>For non-varargs {@code method}, this method expects an array with element count exactly equal to method
	 * parameter count, and with compatible types.
	 *
	 * @param method method that was/will be called on invocation
	 * @param receiver receiver of the method call (i.e. {@code this})
	 * @param arguments arguments in a source representation
	 * @param <T> type of receiver
	 * @return method invocation comprised from passed arguments
	 * @throws IllegalArgumentException when method invocation is illegal and will not succeed: ex. method is static
	 *     and receiver was provided (or other way around), receiver is of wrong type for the provided method,
	 *     or arguments are not matching method parameter types.
	 */
	public static <T> MethodInvocation<T> of(Method method, @Nullable T receiver, Object... arguments) {
		requireNonNull(method);
		// receiver might be null
		requireNonNull(arguments);
		verifyCallability(method, receiver, arguments);
		Object[] argumentsClone = arguments.clone();
		return new MethodInvocation<>(method, receiver, argumentsClone);
	}

	@SuppressWarnings("ArrayIsStoredDirectly")
	private MethodInvocation(Method method, @Nullable T receiver,
							 Object... arguments) {
		this.method = method;
		this.receiver = receiver;
		this.arguments = arguments;
	}

	/**
	 * Executes the configured invocation.
	 *
	 * @return result of non-throwing invocation. If the method was {@code void}, the result will be null.
	 * @throws Throwable result of throwing invocation. This will be exactly the exception that method thrown.
	 */
	@CanIgnoreReturnValue
	@Nullable
	@Override
	public Object invoke() throws Throwable {
		if (handle == null) {
			handle = createHandle();
		}
		return handle.invoke();
	}

	/**
	 * Interface that allows decomposition of the invocation.
	 *
	 * @param <T> type of receiver expected
	 * @param <R> type of result of decomposition.
	 */
	@FunctionalInterface
	public interface Decomposer<T, R> {
		/**
		 * Decomposition method.
		 *
		 * @param method method that was called
		 * @param receiver receiver that the method was called on, or null if the method was static
		 * @param arguments arguments passed to the method, in source (<i>flat</i>) representation
		 * @return result of decomposition
		 */
		R decompose(Method method, @Nullable T receiver, Object... arguments);
	}

	/**
	 * Decomposes the invocation to its parts.
	 *
	 * <p>This method allows to transform this invocation by its parts into other object.
	 *
	 * <p>For example, decomposition might produce log message of method called:
	 * <pre>
	 *     Decomposer&lt;Object, String&gt; stringifingDecomposer = (method, receiver, arguments) -&gt;
	 *         String.format("Method %s was called on %s with %s", method, receiver, arguments);
	 *     LOGGER.debug(invocation.decompose(stringifingDecomposer))
	 * </pre>
	 *
	 * <p>Another example: decomposer might substitute invocation method for another one:
	 * <pre>
	 *     Decomposer&lt;Object, MethodInvocation&lt;?&gt;&gt; replacingDecomposer = (method, receiver, arguments) -&gt;
	 *         MethodInvocation.of(anotherMethod, receiver, arguments);
	 *     MethodInvocation&lt;?&gt; replacedMethodInvocation = invocation.decompose(replacingDecomposer))
	 *     return replacedMethodInvocation.invoke();
	 * </pre>
	 *
	 * @param decomposer decomposer to use for this invocation
	 * @param <R> return type of decomposition
	 * @return whatever decomposer returned on its {@link Decomposer#decompose} call
	 */
	@CanIgnoreReturnValue
	public <R> R decompose(Decomposer<? super T, R> decomposer) {
		return decomposer.decompose(method, receiver, arguments.clone());
	}

	/**
	 * Creates new invocation with replaced method.
	 *
	 * <p>New method is checked for compatibility with both receiver and arguments.
	 *
	 * @param newMethod another method to be used
	 * @return new invocation with new method, same receiver and same arguments
	 * @throws IllegalArgumentException when new method is incompatible with receiver or arguments in any way
	 */
	public MethodInvocation<T> withMethod(Method newMethod) {
		verifyReceiverCompatibility(newMethod, receiver);
		verifyArgumentsCompatibility(newMethod, arguments);
		return new MethodInvocation<>(newMethod, receiver, arguments);
	}

	/**
	 * Creates new invocation with replaced receiver.
	 *
	 * <p>New receiver is checked for compatibility with method.
	 *
	 * @param newReceiver another receiver to be used
	 * @param <X> extension type of the receiver, to allow concretization of result
	 * @return new invocation with same method, new receiver and same arguments
	 * @throws IllegalArgumentException when new receiver is incompatible with method
	 */
	public <X extends T> MethodInvocation<X> withReceiver(X newReceiver) {
		verifyReceiverCompatibility(method, newReceiver);
		return new MethodInvocation<>(method, newReceiver, arguments);
	}

	/**
	 * Creates new invocation with replaced arguments.
	 *
	 * <p>New arguments is checked for compatibility with method.
	 *
	 * @param newArguments new arguments to be used
	 * @return new invocation with same method, same receiver and new arguments
	 * @throws IllegalArgumentException when new arguments is incompatible with method
	 */
	public MethodInvocation<T> withArguments(Object... newArguments) {
		verifyArgumentsCompatibility(method, newArguments);
		return new MethodInvocation<>(method, receiver, newArguments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.method, this.receiver, Arrays.hashCode(this.arguments));
	}

	@Override
	public boolean equals(@Nullable Object obj) {
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

	private static Object[] flattenVariableArguments(Method method, @Nullable Object[] actuals) {
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

	@SuppressWarnings("IllegalCatch")
	private MethodHandle createHandle() {
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
		return MethodHandles.insertArguments(methodHandle, 0, arguments);
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
