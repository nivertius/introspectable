package org.perfectable.introspection.proxy;

import org.perfectable.testable.mockito.MockitoExtension;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.perfectable.introspection.Introspections.introspect;

@ExtendWith(MockitoExtension.class)
public class JdkProxyBuilderTest {

	private static final JdkProxyBuilderFactory FACTORY = new JdkProxyBuilderFactory();

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

		Method FIRST_METHOD = introspect(TestFirstInterface.class).methods().named("firstMethod").parameters().single();

		void firstMethod();

	}

}
