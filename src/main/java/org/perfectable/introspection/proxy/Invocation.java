package org.perfectable.introspection.proxy;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Invocation of execution point.
 *
 * <p>This class represents captured invocation of some element in program with all needed data to proceed.
 * It can be analyzed, replaced or just executed at any time.
 *
 * <p>Depending on the proxy mechanism, this potentially could be any point in program, but most implementations only
 * support capturing method calls into {@link MethodInvocation}.
 *
 * @param <R> Type of value returned by invocation
 * @param <X> Type of exception thrown by invocation
 * @see InvocationHandler
 * @see MethodInvocation
 */
@FunctionalInterface
public interface Invocation<R, X extends Throwable> {
	/**
	 * Invokes the execution point.
	 *
	 * <p>It will either succeed and return a result, possibly null, or fail and throw an exception.
	 *
	 * @return result of an invocation.
	 * @throws X exception that was thrown by invocation
	 */
	R invoke() throws X;

	/**
	 * Creates invocation that calls this one, and casts result to specified type.
	 *
	 * @param <S> type to cast the result
	 * @param castedType representation type to cast the result
	 * @return Invocation that calls this one, and casts result to specified type.
	 */
	default <S> Invocation<S, X> casted(Class<S> castedType) {
		return new Invocations.Casting<>(this, castedType);
	}

	/**
	 * Adapts {@link Runnable} to this interface.
	 *
	 * @param runnable runnable to run
	 * @return invocation that when invoked, will call the runnable and if it doesn't throw runtime exception, it will
	 *     return null.
	 */
	static Invocation<?, RuntimeException> fromRunnable(Runnable runnable) {
		return new Invocations.RunnableAdapter(runnable);
	}

	/**
	 * Adapts {@link Callable} to this interface.
	 *
	 * @param callable runnable to run
	 * @param <R> type returned from callable
	 * @return invocation that when invoked, will call the runnable and if it doesn't throw exception, it will
	 *     return whatever callable execution returns.
	 */
	static <R> Invocation<R, Exception> fromCallable(Callable<R> callable) {
		return new Invocations.CallableAdapter<>(callable);
	}

	/**
	 * Creates invocation that does only one thing: returns the provided argument.
	 *
	 * @param result what the invocation should return
	 * @param <T> type of result returned from invocation
	 * @return invocation that returns provided result.
	 */
	static <T> Invocation<T, RuntimeException> returning(T result) {
		return new Invocations.Returning<>(result);
	}

	/**
	 * Creates invocation that only throws exceptions obtained from supplier.
	 *
	 * <p>Each execution of the invocation will fetch new exception.
	 *
	 * @param <X> type of exception thrown
	 * @param thrownSupplier supplier that will produce exceptions to throw
	 * @return invocation that throws exception
	 */
	static <X extends Throwable> Invocation<?, X> throwing(Supplier<X> thrownSupplier) {
		return new Invocations.Throwing<>(thrownSupplier);
	}

}
