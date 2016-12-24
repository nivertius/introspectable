package org.perfectable.introspection;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("static-method")
public class ReferenceExtractorTest {

	public interface TestInterface {
		Method NO_RESULT_NO_ARGUMENT =
				extractMethod(TestInterface.class, "noResultNoArgument");
		Method NO_RESULT_SINGLE_ARGUMENT =
				extractMethod(TestInterface.class, "noResultSingleArgument", Object.class);
		Method NO_RESULT_DOUBLE_ARGUMENT =
				extractMethod(TestInterface.class, "noResultDoubleArgument", Object.class, Object.class);
		Method NO_RESULT_TRIPLE_ARGUMENT =
				extractMethod(TestInterface.class, "noResultTripleArgument",
						Object.class, Object.class, Object.class);
		Method NO_RESULT_VARARGS_ARGUMENT =
				extractMethod(TestInterface.class, "noResultVarargsArgument", Object[].class);

		Method WITH_RESULT_NO_ARGUMENT =
				extractMethod(TestInterface.class, "withResultNoArgument");
		Method WITH_RESULT_SINGLE_ARGUMENT =
				extractMethod(TestInterface.class, "withResultSingleArgument", Object.class);
		Method WITH_RESULT_DOUBLE_ARGUMENT =
				extractMethod(TestInterface.class, "withResultDoubleArgument", Object.class, Object.class);
		Method WITH_RESULT_TRIPLE_ARGUMENT =
				extractMethod(TestInterface.class, "withResultTripleArgument",
						Object.class, Object.class, Object.class);
		Method WITH_RESULT_VARARGS_ARGUMENT =
				extractMethod(TestInterface.class, "withResultVarargsArgument", Object[].class);

		void noResultNoArgument();

		void noResultSingleArgument(Object argument1);

		void noResultDoubleArgument(Object argument1, Object argument2);

		void noResultTripleArgument(Object argument1, Object argument2, Object argument3);

		void noResultVarargsArgument(Object... arguments);

		Object withResultNoArgument();

		Object withResultSingleArgument(Object argument1);

		Object withResultDoubleArgument(Object argument1, Object argument2);

		Object withResultTripleArgument(Object argument1, Object argument2, Object argument3);

		Object withResultVarargsArgument(Object... arguments);
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
	public void testNoResultTripleArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class).extractTriple(TestInterface::noResultTripleArgument);
		assertThat(extracted).isEqualTo(TestInterface.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	public void testNoResultVarargsArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class).extractVarargs(TestInterface::noResultVarargsArgument);
		assertThat(extracted).isEqualTo(TestInterface.NO_RESULT_VARARGS_ARGUMENT);
	}

	@Test
	public void testWithResultNoArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class).extractNoneFunction(TestInterface::withResultNoArgument);
		assertThat(extracted).isEqualTo(TestInterface.WITH_RESULT_NO_ARGUMENT);
	}

	@Test
	public void testWithResultSingleArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class).extractSingleFunction(TestInterface::withResultSingleArgument);
		assertThat(extracted).isEqualTo(TestInterface.WITH_RESULT_SINGLE_ARGUMENT);
	}

	@Test
	public void testWithResultDoubleArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class).extractDoubleFunction(TestInterface::withResultDoubleArgument);
		assertThat(extracted).isEqualTo(TestInterface.WITH_RESULT_DOUBLE_ARGUMENT);
	}

	@Test
	public void testWithResultTripleArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class).extractTripleFunction(TestInterface::withResultTripleArgument);
		assertThat(extracted).isEqualTo(TestInterface.WITH_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	public void testWithResultVarargsArgument() {
		Method extracted =
				ReferenceExtractor.of(TestInterface.class)
						.extractVarargsFunction(TestInterface::withResultVarargsArgument);
		assertThat(extracted).isEqualTo(TestInterface.WITH_RESULT_VARARGS_ARGUMENT);
	}


	private static Method extractMethod(Class<?> declaringClass, String name, Class<?>... parameterTypes) {
		try {
			return declaringClass.getDeclaredMethod(name, parameterTypes);
		}
		catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		}
	}
}
