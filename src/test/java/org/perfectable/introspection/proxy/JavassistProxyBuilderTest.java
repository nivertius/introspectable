package org.perfectable.introspection.proxy;

class JavassistProxyBuilderTest extends AbstractProxyServiceTest { // SUPPRESS TestClassWithoutTestCases
	@Override
	protected JavassistProxyService createService() {
		return new JavassistProxyService();
	}
}
