package org.perfectable.introspection.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.perfectable.introspection.Methods;

public class JavassistProxyBuilderTest {
	
	private static final JavassistProxyBuilderFactory FACTORY = new JavassistProxyBuilderFactory();
	
	@Rule
	public final MockitoRule rule = MockitoJUnit.rule();
	
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
	
	interface TestFirstInterface {
		
		Method FIRST_METHOD = getMethodSafe(TestFirstInterface.class, "firstMethod");
		
		void firstMethod();
		
	}
	
	static class TestClass {
		@SuppressWarnings("static-method")
		public void classMethod() {
			throw new AssertionError("Actual method should not be called");
		}
	}
	
	static class TestClassMixed extends TestClass implements TestFirstInterface {
		@Override
		public void firstMethod() {
			throw new AssertionError("Actual method should not be called");
		}
	}
	
	public static Method getMethodSafe(Class<?> declaringClass, String methodName, Class<?>... parameterTypes) {
		return Methods.safeExtract(declaringClass, methodName, parameterTypes);
	}
	
}
