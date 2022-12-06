package org.perfectable.introspection.proxy

class JavassistProxyServiceSpec extends AbstractProxyServiceSpec {
	@Override
	protected ProxyService createService() {
		return new JavassistProxyService();
	}
}
