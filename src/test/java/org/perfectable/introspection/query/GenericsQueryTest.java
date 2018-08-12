package org.perfectable.introspection.query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.perfectable.introspection.SimpleReflections.getField;
import static org.perfectable.introspection.SimpleReflections.getMethod;

@SuppressWarnings("ClassCanBeStatic")
class GenericsQueryTest {

	@Nested
	class OfClassTest {

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
	class OfFieldTest {

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

	@Nested
	class OfMethodTest {

		@Test
		void simple() {
			GenericsQuery<?> query = GenericsQuery.of(Unbounded.SIMPLE_METHOD);

			assertThatThrownBy(() -> query.parameter(0))
					.hasNoCause()
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void bound() {
			GenericsQuery<?> query = GenericsQuery.of(Unbounded.TYPE_PARAMETER_METHOD);

			assertThatThrownBy(() -> query.parameter(0))
					.hasNoCause()
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void genericWithParameter() {
			Class<?> result = GenericsQuery.of(Unbounded.OWN_PARAMETER_METHOD)
					.parameter(0)
					.resolve(Unbounded.class);

			assertThat(result)
					.isEqualTo(Object.class);
		}

		@Test
		void genericWithConstant() {
			GenericsQuery<?> query = GenericsQuery.of(Unbounded.GENERIC_CONSTANT_METHOD);

			assertThatThrownBy(() -> query.parameter(0))
					.hasNoCause()
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void genericTypeParameter() {
			GenericsQuery<?> query = GenericsQuery.of(Unbounded.GENERIC_TYPE_PARAMETER_METHOD);

			assertThatThrownBy(() -> query.parameter(0))
					.hasNoCause()
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void genericOwnParameter() {
			Class<?> result = GenericsQuery.of(Unbounded.GENERIC_OWN_PARAMETER_METHOD)
					.parameter(0)
					.resolve(Bounded.class);

			assertThat(result)
					.isEqualTo(CharSequence.class);
		}
	}


	@Nested
	class OfParameterTest {

		@Test
		void simple() {
			GenericsQuery<?> query = GenericsQuery.of(Unbounded.SIMPLE_METHOD.getParameters()[0]);

			assertThatThrownBy(() -> query.parameter(0))
					.hasNoCause()
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void bound() {
			GenericsQuery<?> query = GenericsQuery.of(Unbounded.TYPE_PARAMETER_METHOD.getParameters()[0]);

			assertThatThrownBy(() -> query.parameter(0))
					.hasNoCause()
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void genericWithParameter() {
			GenericsQuery<?> query = GenericsQuery.of(Unbounded.OWN_PARAMETER_METHOD.getParameters()[0]);

			assertThatThrownBy(() -> query.parameter(0))
					.hasNoCause()
					.isInstanceOf(IllegalArgumentException.class);
		}

		@Test
		void genericWithConstant() {
			Class<?> result = GenericsQuery.of(Unbounded.GENERIC_CONSTANT_METHOD.getParameters()[0])
					.parameter(0)
					.resolve(Bounded.class);

			assertThat(result)
					.isEqualTo(String.class);
		}

		@Test
		void genericTypeParameter() {
			Class<?> result = GenericsQuery.of(Unbounded.GENERIC_TYPE_PARAMETER_METHOD.getParameters()[0])
					.parameter(0)
					.resolve(Bounded.class);

			assertThat(result)
					.isEqualTo(Number.class);
		}

		@Test
		void genericOwnParameter() {
			Class<?> result = GenericsQuery.of(Unbounded.GENERIC_OWN_PARAMETER_METHOD.getParameters()[0])
					.parameter(0)
					.resolve(Bounded.class);

			assertThat(result)
					.isEqualTo(CharSequence.class);
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

		static final Method SIMPLE_METHOD =
				getMethod(Unbounded.class, "methodSimple", String.class);
		static final Method TYPE_PARAMETER_METHOD =
				getMethod(Unbounded.class, "methodWithTypeParameter", Number.class);
		static final Method OWN_PARAMETER_METHOD =
				getMethod(Unbounded.class, "methodWithOwnParameter", Object.class);
		static final Method GENERIC_CONSTANT_METHOD =
				getMethod(Unbounded.class, "methodWithGenericConstant", Supplier.class);
		static final Method GENERIC_TYPE_PARAMETER_METHOD =
				getMethod(Unbounded.class, "methodWithGenericTypeParameter", Supplier.class);
		static final Method GENERIC_OWN_PARAMETER_METHOD =
				getMethod(Unbounded.class, "methodWithGenericOwnParameter", Supplier.class);

		String simpleField; // SUPPRESS VisibilityModifier test only

		X boundField; // SUPPRESS VisibilityModifier test only

		Supplier<String> genericWithConstantField; // SUPPRESS VisibilityModifier test only

		Supplier<X> genericWithParameterField; // SUPPRESS VisibilityModifier test only

		abstract void methodSimple(String parameter);

		abstract void methodWithTypeParameter(X parameter);

		abstract <Y> void methodWithOwnParameter(Y parameter);

		abstract void methodWithGenericConstant(Supplier<String> parameter);

		abstract void methodWithGenericTypeParameter(Supplier<X> parameter);

		abstract <Y extends CharSequence> void methodWithGenericOwnParameter(Supplier<Y> parameter);
	}

	abstract static class Bounded implements Root<Long> {
		// test interface
	}

}
