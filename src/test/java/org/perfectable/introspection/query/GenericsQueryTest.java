package org.perfectable.introspection.query;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.perfectable.introspection.SimpleReflections.getField;

public class GenericsQueryTest { // SUPPRESS TestClassWithoutTestCases nested tests only

	@Nested
	static class OfClassTest {

		@Test
		void nonGeneric() {
			GenericsQuery<String> query = GenericsQuery.of(String.class);

			assertThatThrownBy(() -> query.parameter(0))
					.hasNoCause()
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void genericWithoutBounds() {
			Class<?> result = GenericsQuery.of(Root.class).parameter(0)
					.resolve(Unbounded.class);

			assertThat(result)
					.isEqualTo(Number.class);
		}

		@Test
		void genericWithBound() {
			Class<?> result = GenericsQuery.of(Root.class).parameter(0)
					.resolve(Bounded.class);

			assertThat(result)
					.isEqualTo(Long.class);
		}
	}

	@Nested
	static class OfFieldTest {

		@Test
		void simple() {
			GenericsQuery<?> query = GenericsQuery.of(Unbounded.SIMPLE_FIELD);

			assertThatThrownBy(() -> query.parameter(0))
					.hasNoCause()
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void bound() {
			GenericsQuery<?> query = GenericsQuery.of(Unbounded.BOUND_FIELD);

			assertThatThrownBy(() -> query.parameter(0))
					.hasNoCause()
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void genericWithConstantField() {
			Class<?> result = GenericsQuery.of(Unbounded.GENERIC_WITH_CONSTANT_FIELD)
					.parameter(0)
					.resolve(Unbounded.class);

			assertThat(result)
					.isEqualTo(String.class);
		}

		@Test
		void genericWithTypeParameterField() {
			Class<?> result = GenericsQuery.of(Unbounded.GENERIC_WITH_PARAMETER_FIELD)
					.parameter(0)
					.resolve(Unbounded.class);

			assertThat(result)
					.isEqualTo(Number.class);
		}

		@Test
		void genericWithConstantFieldInBoundedSubclass() {
			Class<?> result = GenericsQuery.of(Unbounded.GENERIC_WITH_CONSTANT_FIELD)
					.parameter(0)
					.resolve(Bounded.class);

			assertThat(result)
					.isEqualTo(String.class);
		}

		@Test
		void genericWithTypeParameterFieldInBoundedSubclass() {
			Class<?> result = GenericsQuery.of(Unbounded.GENERIC_WITH_PARAMETER_FIELD)
					.parameter(0)
					.resolve(Bounded.class);

			assertThat(result)
					.isEqualTo(Number.class); // this is Number anyway
		}
	}

	interface Root<X extends Number> {
		// test interface
	}

	abstract static class Unbounded<X extends Number> implements Root<X> {

		static final Field SIMPLE_FIELD = getField(Unbounded.class, "simpleField");
		static final Field BOUND_FIELD = getField(Unbounded.class, "boundField");
		static final Field GENERIC_WITH_CONSTANT_FIELD = getField(Unbounded.class, "genericWithConstantField");
		static final Field GENERIC_WITH_PARAMETER_FIELD = getField(Unbounded.class, "genericWithParameterField");

		String simpleField; // SUPPRESS VisibilityModifier test only

		X boundField; // SUPPRESS VisibilityModifier test only

		Supplier<String> genericWithConstantField; // SUPPRESS VisibilityModifier test only

		Supplier<X> genericWithParameterField; // SUPPRESS VisibilityModifier test only
	}

	abstract static class Bounded implements Root<Long> {
		// test interface
	}

}
