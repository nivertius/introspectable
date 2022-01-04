package org.perfectable.introspection.proxy;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Invocation handler that delegates all calls to actual object.
 *
 * <p>This class is mutable, the target can be swapped while proxies are used.
 *
 * @param <T> type of objects handled
 */
public final class ForwardingHandler<T> implements InvocationHandler<@Nullable Object, Exception, MethodInvocation<T>> {

	private T target;

	/**
	 * Creates handler with specified object as a delegate.
	 *
	 * @param target delegate target
	 * @param <T> type of proxy/target
	 * @return handler that forwards to {@code target}
	 */
	public static <T> ForwardingHandler<T> of(T target) {
		return new ForwardingHandler<>(target);
	}

	private ForwardingHandler(T target) {
		this.target = target;
	}

	/**
	 * Changes target to provided one.
	 *
	 * @param newTarget new delegation target
	 */
	public void swap(T newTarget) {
		this.target = newTarget;
	}

	@Override
	public @Nullable Object handle(MethodInvocation<T> invocation) throws Exception {
		return invocation.withReceiver(target).invoke();
	}

}
