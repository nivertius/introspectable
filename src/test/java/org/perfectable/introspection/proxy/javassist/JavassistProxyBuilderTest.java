package org.perfectable.introspection.proxy.javassist;

import org.perfectable.introspection.MockitoExtension;
import org.perfectable.introspection.proxy.ForwardingHandler;
import org.perfectable.introspection.proxy.ProxyBuilder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JavassistProxyBuilderTest {

	private static final JavassistProxyBuilderFactory FACTORY = new JavassistProxyBuilderFactory();
	private static final String MESSAGE_METHOD_CALLED = "Actual method should not be called";

	@Mock
	private TestFirstInterface firstMock;

	@Mock
	private TestClass classMock;

	@Mock
	private TestClassMixed mixedMock;

	@Test
	void testOfInterfaces() {
		ProxyBuilder<TestFirstInterface> proxyBuilder =
				FACTORY.ofInterfaces(TestFirstInterface.class);
		TestFirstInterface proxy = proxyBuilder.instantiate(ForwardingHandler.of(this.firstMock));

		assertThat(proxy).isInstanceOf(TestFirstInterface.class);
		assertThat(proxy).isNotInstanceOf(TestClass.class);

		this.firstMock.firstMethod();
		proxy.firstMethod();
	}

	@Test
	void testOfClass() {
		ProxyBuilder<TestClass> proxyBuilder =
				FACTORY.ofClass(TestClass.class);
		TestClass proxy = proxyBuilder.instantiate(ForwardingHandler.of(this.classMock));

		assertThat(proxy).isNotInstanceOf(TestFirstInterface.class);
		assertThat(proxy).isInstanceOf(TestClass.class);

		this.classMock.classMethod();
		proxy.classMethod();
	}

	@Test
	void testOfClassWithInterface() {
		ProxyBuilder<TestClass> proxyBuilder =
				FACTORY.ofClass(TestClass.class, TestFirstInterface.class);
		TestClass proxy = proxyBuilder.instantiate(ForwardingHandler.of(this.mixedMock));

		assertThat(proxy).isInstanceOf(TestFirstInterface.class);
		assertThat(proxy).isInstanceOf(TestClass.class);

		this.mixedMock.classMethod();
		proxy.classMethod();

		this.mixedMock.firstMethod();
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
