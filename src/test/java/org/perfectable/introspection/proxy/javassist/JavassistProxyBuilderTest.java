package org.perfectable.introspection.proxy.javassist;

import org.perfectable.introspection.proxy.AbstractProxyBuilderFactoryTest;

class JavassistProxyBuilderTest extends AbstractProxyBuilderFactoryTest { // SUPPRESS TestClassWithoutTestCases
	@Override
	protected JavassistProxyBuilderFactory createFactory() {
		return new JavassistProxyBuilderFactory();
	}
}
