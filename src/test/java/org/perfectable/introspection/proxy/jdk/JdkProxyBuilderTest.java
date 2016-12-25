package org.perfectable.introspection.proxy.jdk;

import org.perfectable.introspection.MockitoExtension;
import org.perfectable.introspection.proxy.ForwardingHandler;
import org.perfectable.introspection.proxy.ProxyBuilder;

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

	public interface TestFirstInterface {

		Method FIRST_METHOD = introspect(TestFirstInterface.class).methods().named("firstMethod").parameters().unique();

		void firstMethod();

	}

}
