package org.perfectable.introspection.proxy;

import org.perfectable.introspection.proxy.ProxyBuilderFactory.Feature;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class FeatureTest {

	@Test
	public void testAny() {
		ProxyBuilderFactory factory = ProxyBuilderFactory.any();
		assertThat(factory)
				.isInstanceOf(org.perfectable.introspection.proxy.jdk.JdkProxyBuilderFactory.class);
	}

	@Test
	public void testEmptyFeatures() {
		ProxyBuilderFactory factory = ProxyBuilderFactory.withFeature();
		assertThat(factory)
				.isNotNull();
	}

	@Test
	public void testSuperclass() {
		ProxyBuilderFactory factory = ProxyBuilderFactory.withFeature(Feature.SUPERCLASS);
		assertThat(factory)
				.isNotNull();
		assertThat(factory.supportsFeature(Feature.SUPERCLASS))
				.isTrue();
	}
}
