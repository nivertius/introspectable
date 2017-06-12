package org.perfectable.introspection.proxy;

import org.perfectable.introspection.proxy.ProxyBuilderFactory.Feature;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FeatureTest {

	@Test
	void testAny() {
		ProxyBuilderFactory factory = ProxyBuilderFactory.any();
		assertThat(factory)
				.isInstanceOf(org.perfectable.introspection.proxy.jdk.JdkProxyBuilderFactory.class);
	}

	@Test
	void testEmptyFeatures() {
		ProxyBuilderFactory factory = ProxyBuilderFactory.withFeature();
		assertThat(factory)
				.isNotNull();
	}

	@Test
	void testSuperclass() {
		ProxyBuilderFactory factory = ProxyBuilderFactory.withFeature(Feature.SUPERCLASS);
		assertThat(factory)
				.isNotNull();
		assertThat(factory.supportsFeature(Feature.SUPERCLASS))
				.isTrue();
	}
}
