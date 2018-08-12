package org.perfectable.introspection.proxy.jdk;

import org.perfectable.introspection.proxy.AbstractProxyBuilderFactoryTest;
import org.perfectable.introspection.proxy.ProxyBuilderFactory;

import org.junit.jupiter.api.Test;

class JdkProxyBuilderTest extends AbstractProxyBuilderFactoryTest {
	@Override
	protected ProxyBuilderFactory createFactory() {
		return new JdkProxyBuilderFactory();
	}

	@Test
	void testOfClass() {
		// test assumption will fail
	}

	@Test
	void testOfClassWithInterface() {
		// test assumption will fail
	}
}
