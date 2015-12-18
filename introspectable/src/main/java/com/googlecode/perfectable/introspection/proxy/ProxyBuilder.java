package com.googlecode.perfectable.introspection.proxy;

public interface ProxyBuilder<I> {
	
	I instantiate(InvocationHandler<I> handler);

	static <I> ProxyBuilder<I> ofInterfaces(Class<I> mainInterface, Class<?>... otherInterfaces) {
		return JdkProxyBuilder.ofInterfaces(mainInterface, otherInterfaces);
	}

	static <I> ProxyBuilder<I> sameAs(I sourceInstance) {
		return JdkProxyBuilder.sameAs(sourceInstance);
	}
}
