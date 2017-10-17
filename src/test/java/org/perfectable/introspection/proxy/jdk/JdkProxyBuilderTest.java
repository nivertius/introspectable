package org.perfectable.introspection.proxy.jdk;

import org.perfectable.introspection.proxy.AbstractProxyBuilderFactoryTest;
import org.perfectable.introspection.proxy.ProxyBuilderFactory;

class JdkProxyBuilderTest extends AbstractProxyBuilderFactoryTest { // SUPPRESS TestClassWithoutTestCases
	@Override
	protected ProxyBuilderFactory createFactory() {
		return new JdkProxyBuilderFactory();
	}
}
