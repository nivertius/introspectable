package org.perfectable.introspection.proxy;


@FunctionalInterface
public interface ProxyBuilder<I> {
	
	I instantiate(InvocationHandler<I> handler);
	
}
