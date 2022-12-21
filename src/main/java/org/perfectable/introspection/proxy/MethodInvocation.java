package org.perfectable.introspection.proxy;

import java.lang.reflect.Method;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Capture of a information needed to invoke a call related to a method.
 *
 * <p>This interface collects and mimics arguments received by different proxy frameworks when method is intercepted on
 * proxy object, represents it in uniform fashion and allows some manipulations. Instances of this type can be also
 * constructed synthetically, as if the call was made but intercepted before execution.
 *
 * <p>This interface handles three elements of a call: Method that was called, a receiver, that is object on which
 * method was called, and arguments that were passed with the call. Receiver can be omitted, if the method is static.
 *
 * <p>There are two most important methods on this class: {@link #invoke} which will execute the method with specified
 * arguments and receiver, and return result or throw an exception. The other method is {@link #decompose} which
 * allows introspection of structure of this invocation.
 *
 * <p>This interface represents a call related to a method, but not necessarily exactly a method call. This call
 * might be intercepted by custom interceptors.
 *
 * <p>Default implementation of this interface are unmodifiable, that is methods of it that would change the
 * object actually * produce new changed object. It is not immutable, because both receiver and arguments passed can,
 * and often are, mutable. It is recommended that when overriding this class, to preserve this property.
 *
 * @param <T> Type of method receiver (i.e. {@code this})
 */
public interface MethodInvocation<T> extends Invocation<@Nullable Object, Exception> {

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
	static <T> MethodInvocation<T> intercepted(Method method, T receiver, @Nullable Object @Nullable... arguments) {
		return ActualMethodInvocation.intercepted(method, receiver, arguments);
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
	static <T> MethodInvocation<T> of(Method method, T receiver, @Nullable Object... arguments) {
		return ActualMethodInvocation.of(method, receiver, arguments);
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
	 * <p>Decomposition will return modifiable arguments array. This shouldn't be a problem in normal use - they are not
	 * usually modified. If you are concerned when passing invocation, you can clone it. This decision was also made to
	 * allow invocation to be compatible with {@link org.aopalliance.intercept.MethodInvocation} from AOP Alliance.
	 *
	 * @param decomposer decomposer to use for this invocation
	 * @param <R> return type of decomposition
	 * @return whatever decomposer returned on its {@link Decomposer#decompose} call
	 */
	@CanIgnoreReturnValue
	<R> R decompose(Decomposer<? super T, R> decomposer);

	/**
	 * Creates new invocation with replaced method.
	 *
	 * <p>New method is checked for compatibility with both receiver and arguments.
	 *
	 * @param newMethod another method to be used
	 * @return new invocation with new method, same receiver and same arguments
	 * @throws IllegalArgumentException when new method is incompatible with receiver or arguments in any way
	 */
	MethodInvocation<T> withMethod(Method newMethod);

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
	<X extends T> MethodInvocation<X> withReceiver(X newReceiver);

	/**
	 * Creates new invocation with replaced arguments.
	 *
	 * <p>New arguments is checked for compatibility with method.
	 *
	 * @param newArguments new arguments to be used
	 * @return new invocation with same method, same receiver and new arguments
	 * @throws IllegalArgumentException when new arguments is incompatible with method
	 */
	MethodInvocation<T> withArguments(Object... newArguments);

	/**
	 * Interface that allows decomposition of the invocation.
	 *
	 * @param <T> type of receiver expected
	 * @param <R> type of result of decomposition.
	 */
	@FunctionalInterface
	interface Decomposer<T, R> {
		/**
		 * Decomposition method.
		 *
		 * @param method method that was called
		 * @param receiver receiver that the method was called on, or null if the method was static
		 * @param arguments arguments passed to the method, in source (<i>flat</i>) representation. In this library,
		 *                  arguments are returned as-is, without cloning, so they can be modified.
		 * @return result of decomposition
		 */
		R decompose(Method method, T receiver, @Nullable Object... arguments);
	}



}
