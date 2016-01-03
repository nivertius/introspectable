package com.googlecode.perfectable.introspection;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;

import com.googlecode.perfectable.introspection.proxy.Invocable;
import com.googlecode.perfectable.introspection.proxy.MethodInvocable;

@SuppressWarnings("static-method")
public class ReferenceExtractorTest {
	
	interface TestInterface {
		Invocable NO_RESULT_NO_ARGUMENT =
				extractInvocable(TestInterface.class, "noResultNoArgument");
		Invocable NO_RESULT_SINGLE_ARGUMENT =
				extractInvocable(TestInterface.class, "noResultSingleArgument", Object.class);
		Invocable NO_RESULT_DOUBLE_ARGUMENT =
				extractInvocable(TestInterface.class, "noResultDoubleArgument", Object.class, Object.class);
		
		Invocable WITH_RESULT_NO_ARGUMENT =
				extractInvocable(TestInterface.class, "withResultNoArgument");
		Invocable WITH_RESULT_SINGLE_ARGUMENT =
				extractInvocable(TestInterface.class, "withResultSingleArgument", Object.class);
		Invocable WITH_RESULT_DOUBLE_ARGUMENT =
				extractInvocable(TestInterface.class, "withResultDoubleArgument", Object.class, Object.class);
		
		void noResultNoArgument();
		
		void noResultSingleArgument(Object argument1);
		
		void noResultDoubleArgument(Object argument1, Object argument2);
		
		Object withResultNoArgument();
		
		Object withResultSingleArgument(Object argument1);
		
		Object withResultDoubleArgument(Object argument1, Object argument2);
		
	}
	
	@Test
	public void testNoResultNoArgument() {
		Invocable extracted =
				ReferenceExtractor.of(TestInterface.class).extractNone(TestInterface::noResultNoArgument);
		assertThat(extracted).isEqualTo(TestInterface.NO_RESULT_NO_ARGUMENT);
	}
	
	@Test
	public void testNoResultSingleArgument() {
		Invocable extracted =
				ReferenceExtractor.of(TestInterface.class).extractSingle(TestInterface::noResultSingleArgument);
		assertThat(extracted).isEqualTo(TestInterface.NO_RESULT_SINGLE_ARGUMENT);
	}
	
	@Test
	public void testNoResultDoubleArgument() {
		Invocable extracted =
				ReferenceExtractor.of(TestInterface.class).extractDouble(TestInterface::noResultDoubleArgument);
		assertThat(extracted).isEqualTo(TestInterface.NO_RESULT_DOUBLE_ARGUMENT);
	}
	
	@Test
	public void testWithResultNoArgument() {
		Invocable extracted =
				ReferenceExtractor.of(TestInterface.class).extractNone(TestInterface::withResultNoArgument);
		assertThat(extracted).isEqualTo(TestInterface.WITH_RESULT_NO_ARGUMENT);
	}
	
	@Test
	public void testWithResultSingleArgument() {
		Invocable extracted =
				ReferenceExtractor.of(TestInterface.class).extractSingle(TestInterface::withResultSingleArgument);
		assertThat(extracted).isEqualTo(TestInterface.WITH_RESULT_SINGLE_ARGUMENT);
	}
	
	@Test
	public void testWithResultDoubleArgument() {
		Invocable extracted =
				ReferenceExtractor.of(TestInterface.class).extractDouble(TestInterface::withResultDoubleArgument);
		assertThat(extracted).isEqualTo(TestInterface.WITH_RESULT_DOUBLE_ARGUMENT);
	}
	
	private static Invocable extractInvocable(Class<?> declaringClass, String name, Class<?>... parameterTypes) {
		Method method;
		try {
			method = declaringClass.getDeclaredMethod(name, parameterTypes);
		}
		catch(NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		}
		return MethodInvocable.of(method);
	}
}
