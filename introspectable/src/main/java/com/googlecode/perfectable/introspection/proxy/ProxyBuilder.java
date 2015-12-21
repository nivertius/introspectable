package com.googlecode.perfectable.introspection.proxy;

public interface ProxyBuilder<I> {
	
	I instantiate(InvocationHandler<I> handler);
	
	static ProxyBuilder<?> ofInterfaces(Class<?>[] interfaces) {
		return JdkProxyBuilder.ofInterfaces(interfaces);
	}
	
	static <I> ProxyBuilder<I> ofInterfacesOf(Class<I> implementingClass) {
		return JdkProxyBuilder.ofInterfacesOf(implementingClass);
	}
	
	static <I> ProxyBuilder<I> ofInterfaces(Class<I> mainInterface, Class<?>... otherInterfaces) {
		return JdkProxyBuilder.ofInterfaces(mainInterface, otherInterfaces);
	}
	
	static <I> ProxyBuilder<I> sameAs(I sourceInstance) {
		return JdkProxyBuilder.sameAs(sourceInstance);
	}
}
