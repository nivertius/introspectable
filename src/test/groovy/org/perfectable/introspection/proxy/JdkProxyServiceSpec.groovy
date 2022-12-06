package org.perfectable.introspection.proxy

class JdkProxyServiceSpec extends AbstractProxyServiceSpec {
	@Override
	protected ProxyService createService() {
		return new JdkProxyService()
	}
}
