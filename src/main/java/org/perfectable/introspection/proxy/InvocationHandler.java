package org.perfectable.introspection.proxy;

import javax.annotation.Nullable;

/**
 * Handles an invocation of a execution point on a proxy and returns response.
 *
 * <p>This is main interface to be implemented when creating proxies. Every call on the proxy will be passed to
 * {@link #handle}, and result of this method will be returned by proxy.
 *
 * @param <I> type of invocation supported. This is usually {@link MethodInvocation}

 * @see ProxyBuilder#instantiate
 * @see MethodInvocation
 */
@FunctionalInterface
public interface InvocationHandler<I extends Invocation> {

	/**
	 * Catches invocation executed on proxy and returns result that should be passed to the client.
	 *
	 * @param invocation invocation captured
	 * @return successful result of invocation processing
	 * @throws Throwable exception that will be thrown on the call site
	 */
	@Nullable
	Object handle(I invocation) throws Throwable; // SUPPRESS IllegalThrows
}
