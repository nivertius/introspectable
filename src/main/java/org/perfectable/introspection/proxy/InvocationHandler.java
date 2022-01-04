package org.perfectable.introspection.proxy;

/**
 * Handles an invocation of a execution point on a proxy and returns response.
 *
 * <p>This is main interface to be implemented when creating proxies. Every call on the proxy will be passed to
 * {@link #handle}, and result of this method will be returned by proxy.
 *
 * @param <I> type of invocation supported. This is usually {@link MethodInvocation}

 * @param <R> type of values returned from handler
 * @param <X> type of exceptions thrown from handler
 * @see ProxyBuilder#instantiate
 * @see MethodInvocation
 */
@FunctionalInterface
public interface InvocationHandler<R, X extends Exception, I extends Invocation<?, ?>> {

	/**
	 * Catches invocation executed on proxy and returns result that should be passed to the client.
	 *
	 * @param invocation invocation captured
	 * @return successful result of invocation processing
	 * @throws X exception that will be thrown on the call site
	 */
	R handle(I invocation) throws X;
}
