package org.perfectable.introspection.proxy;

import java.io.IOException;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

// SUPPRESS FILE MagicNumber
// SUPPRESS FILE MultipleStringLiterals
public abstract class AbstractProxyBuilderFactoryTest {

	private static final String MESSAGE_ASSUME_SUPERCLASS = "This factory does not support superclass proxies";
	private static final String MESSAGE_METHOD_CALLED = "Actual method should not be called";

	protected abstract ProxyBuilderFactory createFactory();

	@Test
	void testVarargSimple() throws IOException {
		ProxyBuilderFactory factory = createFactory();
		ProxyBuilder<TestVarargsInterface> proxyBuilder =
			factory.ofInterfaces(TestVarargsInterface.class);
		TestHandler<TestVarargsInterface> handler = TestHandler.create();

		TestVarargsInterface proxy = proxyBuilder.instantiate(handler);

		assertThat(proxy).isInstanceOf(TestVarargsInterface.class);

		String firstArgument = "one";
		String secondArgument = "two";
		String thirdArgument = "three";
		String expectedResult = "result";
		handler.expectInvocation(proxy, TestVarargsInterface.METHOD, firstArgument, secondArgument, thirdArgument)
			.andReturn(expectedResult);

		String result = proxy.variableMethod(firstArgument, secondArgument, thirdArgument);

		assertThat(result)
			.isEqualTo(expectedResult);

		handler.verify();
	}

	@Test
	void testVarargPrimitive() throws IOException {
		ProxyBuilderFactory factory = createFactory();
		ProxyBuilder<TestVarargsPrimitiveInterface> proxyBuilder =
			factory.ofInterfaces(TestVarargsPrimitiveInterface.class);
		TestHandler<TestVarargsPrimitiveInterface> handler = TestHandler.create();

		TestVarargsPrimitiveInterface proxy = proxyBuilder.instantiate(handler);

		assertThat(proxy).isInstanceOf(TestVarargsPrimitiveInterface.class);

		String expectedResult = "result";
		String firstArgument = "one";
		int secondArgument = 231;
		int thirdArgument = 234;
		handler
			.expectInvocation(proxy, TestVarargsPrimitiveInterface.METHOD, firstArgument, secondArgument, thirdArgument)
			.andReturn(expectedResult);

		String result = proxy.variablePrimitive(firstArgument, secondArgument, thirdArgument);

		assertThat(result)
			.isEqualTo(expectedResult);

		handler.verify();
	}

	@Test
	void testCheckedException() throws IOException {
		ProxyBuilderFactory factory = createFactory();
		ProxyBuilder<TestInterfaceChecked> proxyBuilder =
			factory.ofInterfaces(TestInterfaceChecked.class);
		TestHandler<TestInterfaceChecked> handler = TestHandler.create();
		TestInterfaceChecked proxy = proxyBuilder.instantiate(handler);

		assertThat(proxy).isInstanceOf(TestInterfaceChecked.class);

		IOException thrown = new IOException();
		handler.expectInvocation(proxy, TestInterfaceChecked.METHOD)
			.andThrow(thrown);

		assertThatThrownBy(() -> proxy.checkedMethod())
			.isSameAs(thrown);

		handler.verify();
	}

	@Test
	void testUncheckedException() throws IOException {
		ProxyBuilderFactory factory = createFactory();
		ProxyBuilder<TestFirstInterface> proxyBuilder =
			factory.ofInterfaces(TestFirstInterface.class);
		TestHandler<TestFirstInterface> handler = TestHandler.create();
		TestFirstInterface proxy = proxyBuilder.instantiate(handler);

		assertThat(proxy).isInstanceOf(TestFirstInterface.class);

		IllegalArgumentException thrown = new IllegalArgumentException();
		handler.expectInvocation(proxy, TestFirstInterface.METHOD)
			.andThrow(thrown);

		assertThatThrownBy(() -> proxy.firstMethod())
			.isSameAs(thrown);

		handler.verify();
	}

	@Test
	void testOfInterfaces() {
		ProxyBuilderFactory factory = createFactory();
		ProxyBuilder<TestFirstInterface> proxyBuilder =
			factory.ofInterfaces(TestFirstInterface.class);
		TestHandler<TestFirstInterface> handler = TestHandler.create();
		TestFirstInterface proxy = proxyBuilder.instantiate(handler);

		assertThat(proxy).isInstanceOf(TestFirstInterface.class);
		assertThat(proxy).isNotInstanceOf(TestClass.class);

		handler.expectInvocation(proxy, TestFirstInterface.METHOD)
			.andReturn(null);

		proxy.firstMethod();

		handler.verify();
	}

	@Test
	void testOfClass() {
		ProxyBuilderFactory factory = createFactory();
		assumeTrue(factory.supportsFeature(ProxyBuilderFactory.Feature.SUPERCLASS), MESSAGE_ASSUME_SUPERCLASS);

		ProxyBuilder<TestClass> proxyBuilder =
			factory.ofClass(TestClass.class);
		TestHandler<TestClass> handler = TestHandler.create();
		TestClass proxy = proxyBuilder.instantiate(handler);

		assertThat(proxy).isNotInstanceOf(TestFirstInterface.class);
		assertThat(proxy).isInstanceOf(TestClass.class);

		handler.expectInvocation(proxy, TestClass.METHOD)
			.andReturn(null);

		proxy.classMethod();

		handler.verify();
	}

	@Test
	void testOfClassWithInterface() {
		ProxyBuilderFactory factory = createFactory();
		assumeTrue(factory.supportsFeature(ProxyBuilderFactory.Feature.SUPERCLASS), MESSAGE_ASSUME_SUPERCLASS);
		ProxyBuilder<TestClass> proxyBuilder =
			factory.ofClass(TestClass.class, TestFirstInterface.class);
		TestHandler<TestClass> handler = TestHandler.create();
		TestClass proxy = proxyBuilder.instantiate(handler);

		assertThat(proxy).isInstanceOf(TestFirstInterface.class);
		assertThat(proxy).isInstanceOf(TestClass.class);

		handler.expectInvocation(proxy, TestClass.METHOD)
			.andReturn(null);
		proxy.classMethod();

		handler.expectInvocation(proxy, TestFirstInterface.METHOD)
			.andReturn(null);
		((TestFirstInterface) proxy).firstMethod();

		handler.verify();
	}

	public interface TestInterfaceChecked {
		Method METHOD = extractMethod(TestInterfaceChecked.class, "checkedMethod");

		void checkedMethod() throws IOException;
	}

	public interface TestFirstInterface {
		Method METHOD = extractMethod(TestFirstInterface.class, "firstMethod");

		void firstMethod();
	}

	public interface TestVarargsInterface {
		String variableMethod(String required, String... variable);

		Method METHOD = extractMethod(TestVarargsInterface.class, "variableMethod", String.class, String[].class);

	}

	public interface TestVarargsPrimitiveInterface {
		String variablePrimitive(String required, int... variable);

		Method METHOD =
			extractMethod(TestVarargsPrimitiveInterface.class, "variablePrimitive", String.class, int[].class);

	}

	public static class TestClass {
		public static final Method METHOD = extractMethod(TestClass.class, "classMethod");

		public void classMethod() {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}
	}

	static Method extractMethod(Class<?> declaringClass, String methodName, Class<?>... argumentTypes) {
		try {
			return declaringClass.getDeclaredMethod(methodName, argumentTypes);
		}
		catch (NoSuchMethodException e) {
			throw new AssertionError(e);
		}
	}
}
