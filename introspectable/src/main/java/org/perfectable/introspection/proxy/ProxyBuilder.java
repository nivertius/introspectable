package org.perfectable.introspection.proxy;


public interface ProxyBuilder<I> {
	
	I instantiate(InvocationHandler<I> handler);
	
}
