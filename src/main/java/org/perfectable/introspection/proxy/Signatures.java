package org.perfectable.introspection.proxy;

import org.perfectable.introspection.FunctionalReference;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Collects possible method signatures, to use for binding in {@link InvocationHandlerBuilder}.
 *
 * <p>This class contains declarations of possible instance method signatures with combinations
 * of returning/not returning value and argument count and type.
 * Conventions are "Procedure" doesn't have a result (is void) and "Function" does have result
 * (as in Ada programming language). After name there is a number of arguments.
 *
 * @see InvocationHandlerBuilder
 */
@SuppressWarnings("FunctionalInterfaceMethodChanged")
public final class Signatures {

	/**
	 * Marks signature that can be partially applied.
	 *
	 * <p>Currying is a process of setting one argument as local constant in function to get function with one less
	 * parameter.
	 *
	 * @param <A> Type of argument that can be removed. This is always first argument of a function/procedure.
	 * @param <R> Result of currying, a function or procedure with one less parameter.
	 */
	@FunctionalInterface
	public interface Curryable<A, R> {
		/**
		 * Applies first argument and returns partially applied function/procedure.
		 *
		 * @param argument1 Argument to be fixed in result
		 * @return Resulting partial signature
		 */
		R curry(A argument1);
	}

	/**
	 * Marks signature as convertible to Invocation.
	 *
	 * @param <R> Result of an invocation
	 * @param <X> Exception thrown by invocation
	 */
	public interface InvocationConvertible<R, X extends Throwable> {
		/**
		 * Curries required arguments invocation.
		 *
		 * @param arguments arguments to be fixed in the call
		 * @return Invocation calling the function/procedure with provided arguments.
		 * @throws IllegalArgumentException if there are invalid amount of arguments provided
		 */
		Invocation<R, X> toInvocation(@Nullable Object... arguments);
	}

	@SuppressWarnings("javadoc")
	@FunctionalInterface
	public interface Procedure0<X extends Throwable> extends InvocationConvertible<Void, X>, FunctionalReference {
		void call() throws X;

		@Override
		default Invocation<Void, X> toInvocation(@Nullable Object... arguments) {
			checkArguments(arguments, 0);
			return () -> {
				call();
				return null;
			};
		}
	}

	@SuppressWarnings("javadoc")
	@FunctionalInterface
	public interface Procedure1<P1, X extends Throwable>
		extends Curryable<P1, Procedure0<X>>, InvocationConvertible<Void, X>, FunctionalReference {

		void call(P1 argument1) throws X;

		@Override
		default Procedure0<X> curry(P1 argument1) {
			return () -> call(argument1);
		}

		@SuppressWarnings({"unchecked", "assignment.type.incompatible"})
		@Override
		default Invocation<Void, X> toInvocation(@Nullable Object... arguments) {
			checkArguments(arguments, 1);
			P1 argument1 = (P1) arguments[0];
			return () -> {
				call(argument1);
				return null;
			};
		}
	}

	@SuppressWarnings("javadoc")
	@FunctionalInterface
	public interface Procedure2<P1, P2, X extends Throwable>
		extends Curryable<P1, Procedure1<P2, X>>, InvocationConvertible<Void, X>, FunctionalReference {
		void call(P1 argument1, P2 argument2) throws X;

		@Override
		default Procedure1<P2, X> curry(P1 argument1) {
			return argument2 -> call(argument1, argument2);
		}

		@SuppressWarnings({"unchecked", "assignment.type.incompatible"})
		@Override
		default Invocation<Void, X> toInvocation(@Nullable Object... arguments) {
			checkArguments(arguments, 2);
			P1 argument1 = (P1) arguments[0];
			P2 argument2 = (P2) arguments[1];
			return () -> {
				call(argument1, argument2);
				return null;
			};
		}
	}

	@SuppressWarnings("javadoc")
	@FunctionalInterface
	public interface Function0<R, X extends Throwable>
		extends InvocationConvertible<R, X>, FunctionalReference {
		R call() throws X;

		@Override
		default Invocation<R, X> toInvocation(@Nullable Object... arguments) {
			checkArguments(arguments, 0);
			return () -> call();
		}
	}

	@SuppressWarnings("javadoc")
	@FunctionalInterface
	public interface Function1<R, P1, X extends Throwable>
		extends Curryable<P1, Function0<R, X>>, InvocationConvertible<R, X>, FunctionalReference {

		R call(P1 argument1) throws X;

		@Override
		default Function0<R, X> curry(P1 argument1) {
			return () -> call(argument1);
		}

		@SuppressWarnings({"unchecked", "assignment.type.incompatible"})
		@Override
		default Invocation<R, X> toInvocation(@Nullable Object... arguments) {
			checkArguments(arguments, 1);
			P1 argument1 = (P1) arguments[0];
			return () -> call(argument1);
		}
	}

	@SuppressWarnings("javadoc")
	@FunctionalInterface
	public interface Function2<R, P1, P2, X extends Throwable>
		extends Curryable<P1, Function1<R, P2, X>>, InvocationConvertible<R, X>, FunctionalReference {
		R call(P1 argument1, P2 argument2) throws X;

		@Override
		default Function1<R, P2, X> curry(P1 argument1) {
			return argument2 -> call(argument1, argument2);
		}

		@SuppressWarnings({"unchecked", "assignment.type.incompatible"})
		@Override
		default Invocation<R, X> toInvocation(@Nullable Object... arguments) {
			checkArguments(arguments, 2);
			P1 argument1 = (P1) arguments[0];
			P2 argument2 = (P2) arguments[1];
			return () -> call(argument1, argument2);
		}
	}

	private static void checkArguments(@Nullable Object[] arguments, int requiredLength) {
		if (arguments.length != requiredLength) {
			throw new IllegalArgumentException();
		}
	}

	private Signatures() {
		// utility class
	}
}
