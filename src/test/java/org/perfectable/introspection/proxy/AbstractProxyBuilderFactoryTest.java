package org.perfectable.introspection.proxy;

import org.perfectable.introspection.MockitoExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(MockitoExtension.class)
public abstract class AbstractProxyBuilderFactoryTest {

	private static final String MESSAGE_ASSUME_SUPERCLASS = "This factory does not support superclass proxies";
	private static final String MESSAGE_METHOD_CALLED = "Actual method should not be called";

	protected abstract ProxyBuilderFactory createFactory();

	@Test
	void testOfInterfaces(@Mock TestFirstInterface firstMock) {
		ProxyBuilderFactory factory = createFactory();
		ProxyBuilder<TestFirstInterface> proxyBuilder =
			factory.ofInterfaces(TestFirstInterface.class);
		TestFirstInterface proxy = proxyBuilder.instantiate(ForwardingHandler.of(firstMock));

		assertThat(proxy).isInstanceOf(TestFirstInterface.class);
		assertThat(proxy).isNotInstanceOf(TestClass.class);

		firstMock.firstMethod();
		proxy.firstMethod();
	}

	@Test
	void testOfClass(@Mock TestClass classMock) {
		ProxyBuilderFactory factory = createFactory();
		assumeTrue(factory.supportsFeature(ProxyBuilderFactory.Feature.SUPERCLASS), MESSAGE_ASSUME_SUPERCLASS);

		ProxyBuilder<TestClass> proxyBuilder =
			factory.ofClass(TestClass.class);
		TestClass proxy = proxyBuilder.instantiate(ForwardingHandler.of(classMock));

		assertThat(proxy).isNotInstanceOf(TestFirstInterface.class);
		assertThat(proxy).isInstanceOf(TestClass.class);

		classMock.classMethod();
		proxy.classMethod();
	}

	@Test
	void testOfClassWithInterface(@Mock TestClassMixed mixedMock) {
		ProxyBuilderFactory factory = createFactory();
		assumeTrue(factory.supportsFeature(ProxyBuilderFactory.Feature.SUPERCLASS), MESSAGE_ASSUME_SUPERCLASS);
		ProxyBuilder<TestClass> proxyBuilder =
			factory.ofClass(TestClass.class, TestFirstInterface.class);
		TestClass proxy = proxyBuilder.instantiate(ForwardingHandler.of(mixedMock));

		assertThat(proxy).isInstanceOf(TestFirstInterface.class);
		assertThat(proxy).isInstanceOf(TestClass.class);

		mixedMock.classMethod();
		proxy.classMethod();

		mixedMock.firstMethod();
		((TestFirstInterface) proxy).firstMethod();
	}

	public interface TestFirstInterface {
		void firstMethod();
	}

	public static class TestClass {
		public void classMethod() {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}
	}

	public static class TestClassMixed extends TestClass implements TestFirstInterface {
		@Override
		public void firstMethod() {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}
	}
}
