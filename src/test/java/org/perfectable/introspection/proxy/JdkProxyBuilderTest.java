package org.perfectable.introspection.proxy;

class JdkProxyBuilderTest extends AbstractProxyServiceTest { // SUPPRESS TestClassWithoutTestCases
	@Override
	protected ProxyService createService() {
		return new JdkProxyService();
	}
}
