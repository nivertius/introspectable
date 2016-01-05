package com.googlecode.perfectable.introspection;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.junit.Test;

@SuppressWarnings("static-method")
public class ReferenceExtractorTest {
	
	interface TestInterface {
		Method NO_RESULT_NO_ARGUMENT =
				extractMethod(TestInterface.class, "noResultNoArgument");
		Method NO_RESULT_SINGLE_ARGUMENT =
				extractMethod(TestInterface.class, "noResultSingleArgument", Object.class);
		Method NO_RESULT_DOUBLE_ARGUMENT =
				extractMethod(TestInterface.class, "noResultDoubleArgument", Object.class, Object.class);
		
		Method WITH_RESULT_NO_ARGUMENT =
				extractMethod(TestInterface.class, "withResultNoArgument");
		Method WITH_RESULT_SINGLE_ARGUMENT =
				extractMethod(TestInterface.class, "withResultSingleArgument", Object.class);
		Method WITH_RESULT_DOUBLE_ARGUMENT =
				extractMethod(TestInterface.class, "withResultDoubleArgument", Object.class, Object.class);
		
		void noResultNoArgument();
		
		void noResultSingleArgument(Object argument1);
		
		void noResultDoubleArgument(Object argument1, Object argument2);
		
		Object withResultNoArgument();
		
		Object withResultSingleArgument(Object argument1);
		
		Object withResultDoubleArgument(Object argument1, Object argument2);
		
	}
	
	@Test
	public void testNoResultNoArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class).extractNone(TestInterface::noResultNoArgument);
		assertThat(extracted).isEqualTo(TestInterface.NO_RESULT_NO_ARGUMENT);
	}
	
	@Test
	public void testNoResultSingleArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class).extractSingle(TestInterface::noResultSingleArgument);
		assertThat(extracted).isEqualTo(TestInterface.NO_RESULT_SINGLE_ARGUMENT);
	}
	
	@Test
	public void testNoResultDoubleArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class).extractDouble(TestInterface::noResultDoubleArgument);
		assertThat(extracted).isEqualTo(TestInterface.NO_RESULT_DOUBLE_ARGUMENT);
	}
	
	@Test
	public void testWithResultNoArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class).extractNone(TestInterface::withResultNoArgument);
		assertThat(extracted).isEqualTo(TestInterface.WITH_RESULT_NO_ARGUMENT);
	}
	
	@Test
	public void testWithResultSingleArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class).extractSingle(TestInterface::withResultSingleArgument);
		assertThat(extracted).isEqualTo(TestInterface.WITH_RESULT_SINGLE_ARGUMENT);
	}
	
	@Test
	public void testWithResultDoubleArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class).extractDouble(TestInterface::withResultDoubleArgument);
		assertThat(extracted).isEqualTo(TestInterface.WITH_RESULT_DOUBLE_ARGUMENT);
	}
	
	private static Method extractMethod(Class<?> declaringClass, String name, Class<?>... parameterTypes) {
		try {
			return declaringClass.getDeclaredMethod(name, parameterTypes);
		}
		catch(NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		}
	}
}
