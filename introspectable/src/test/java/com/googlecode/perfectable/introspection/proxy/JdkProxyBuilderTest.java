package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.googlecode.perfectable.introspection.Methods;

public class JdkProxyBuilderTest {
	
	private static final JdkProxyBuilderFactory FACTORY = new JdkProxyBuilderFactory();
	
	@Rule
	public final MockitoRule rule = MockitoJUnit.rule();
	
	@Mock
	private TestFirstInterface firstMock;
	
	@Test
	public void testSimple() {
		ProxyBuilder<TestFirstInterface> proxyBuilder =
				FACTORY.ofInterfaces(TestFirstInterface.class);
		TestFirstInterface proxy = proxyBuilder.instantiate(ForwardingHandler.of(this.firstMock));
		this.firstMock.firstMethod();
		proxy.firstMethod();
	}
	
	interface TestFirstInterface {
		
		Method FIRST_METHOD = Methods.safeExtract(TestFirstInterface.class, "firstMethod");
		
		void firstMethod();
		
	}
	
}
