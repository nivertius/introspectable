package org.perfectable.introspection.proxy;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Invocation of execution point.
 *
 * <p>This class represents captured invocation of some element in program with all needed data to proceed.
 * It can be analyzed, replaced or just executed at any time.
 *
 * <p>Depending on the proxy mechanism, this potentially could be any point in program, but most implementations only
 * support capturing method calls into {@link MethodInvocation}.
 *
 * @see InvocationHandler
 * @see MethodInvocation
 */
@FunctionalInterface
public interface Invocation {
	/**
	 * Invokes the execution point.
	 *
	 * <p>It will either succeed and return a result, possibly null, or fail and throw an exception.
	 *
	 * @return result of an invocation.
	 * @throws Throwable exception that was thrown by invocation
	 */
	@SuppressWarnings({"IllegalThrows", "AnnotationLocation"})
	@Nullable Object invoke() throws Throwable;

	/**
	 * Adapts {@link Runnable} to this interface.
	 *
	 * @param runnable runnable to run
	 * @return invocation that when invoked, will call the runnable and if it doesn't throw runtime exception, it will
	 *     return null.
	 */
	static Invocation fromRunnable(Runnable runnable) {
		return new Invocations.RunnableAdapter(runnable);
	}

	/**
	 * Adapts {@link Callable} to this interface.
	 *
	 * @param callable runnable to run
	 * @return invocation that when invoked, will call the runnable and if it doesn't throw exception, it will
	 *     return whatever callable execution returns.
	 */
	static Invocation fromCallable(Callable<?> callable) {
		return new Invocations.CallableAdapter(callable);
	}

	/**
	 * Creates invocation that does only one thing: returns the provided argument.
	 *
	 * @param result what the invocation should return
	 * @return invocation that returns provided result.
	 */
	static Invocation returning(@Nullable Object result) {
		return new Invocations.Returning(result);
	}

	/**
	 * Creates invocation that only throws exceptions obtained from supplier.
	 *
	 * <p>Each execution of the invocation will fetch new exception.
	 *
	 * @param thrownSupplier supplier that will produce exceptions to throw
	 * @return invocation that throws exception
	 */
	static Invocation throwing(Supplier<Throwable> thrownSupplier) {
		return new Invocations.Throwing(thrownSupplier);
	}

}
