package org.perfectable.introspection.proxy.javassist;

import org.perfectable.introspection.MockitoExtension;
import org.perfectable.introspection.proxy.ForwardingHandler;
import org.perfectable.introspection.proxy.ProxyBuilder;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.perfectable.introspection.Introspections.introspect;

@ExtendWith(MockitoExtension.class)
public class JavassistProxyBuilderTest {

	private static final JavassistProxyBuilderFactory FACTORY = new JavassistProxyBuilderFactory();

	@Mock
	private TestFirstInterface firstMock;

	@Mock
	private TestClass classMock;

	@Mock
	private TestClassMixed mixedMock;

	@Test
	public void testOfInterfaces() {
		ProxyBuilder<TestFirstInterface> proxyBuilder =
				FACTORY.ofInterfaces(TestFirstInterface.class);
		TestFirstInterface proxy = proxyBuilder.instantiate(ForwardingHandler.of(this.firstMock));

		assertThat(proxy).isInstanceOf(TestFirstInterface.class);
		assertThat(proxy).isNotInstanceOf(TestClass.class);

		this.firstMock.firstMethod();
		proxy.firstMethod();
	}

	@Test
	public void testOfClass() {
		ProxyBuilder<TestClass> proxyBuilder =
				FACTORY.ofClass(TestClass.class);
		TestClass proxy = proxyBuilder.instantiate(ForwardingHandler.of(this.classMock));

		assertThat(proxy).isNotInstanceOf(TestFirstInterface.class);
		assertThat(proxy).isInstanceOf(TestClass.class);

		this.classMock.classMethod();
		proxy.classMethod();
	}

	@Test
	public void testOfClassWithInterface() {
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

		Method FIRST_METHOD = introspect(TestFirstInterface.class).methods().named("firstMethod").parameters().unique();

		void firstMethod();

	}

	public static class TestClass {
		public void classMethod() {
			throw new AssertionError("Actual method should not be called");
		}
	}

	public static class TestClassMixed extends TestClass implements TestFirstInterface {
		@Override
		public void firstMethod() {
			throw new AssertionError("Actual method should not be called");
		}
	}

}
