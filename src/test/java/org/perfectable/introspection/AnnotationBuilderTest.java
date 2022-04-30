package org.perfectable.introspection; // SUPPRESS LENGTH

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// SUPPRESS FILE MagicNumber
// SUPPRESS FILE MultipleStringLiterals
@SuppressWarnings({"ClassCanBeStatic", "argument", "assignment"})
class AnnotationBuilderTest {

	@Test
	void notAnInterface() {
		@SuppressWarnings("unchecked")
		Class<Annotation> falseAnnotationClass = (Class<Annotation>) (Class<?>) String.class;
		assertThatThrownBy(() -> AnnotationBuilder.marker(falseAnnotationClass))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Provided class is not an annotation interface");
	}

	@Test
	void notAnAnnotationInterface() {
		@SuppressWarnings("unchecked")
		Class<Annotation> falseAnnotationClass = (Class<Annotation>) (Class<?>) Serializable.class;
		assertThatThrownBy(() -> AnnotationBuilder.marker(falseAnnotationClass))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Provided class is not an annotation interface");
	}

	@Test
	void invalidMarker() {
		assertThatThrownBy(() -> AnnotationBuilder.marker(Single.class))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Annotation interface is not a marker");
	}

	@SuppressWarnings("argument.type.incompatible")
	@Test
	void nullValue() {
		AnnotationBuilder<Single> builder = AnnotationBuilder.of(Single.class);

		assertThatThrownBy(() -> builder.with(Single::value, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Null cannot be provided for member value of type class java.lang.String");
	}

	@Test
	void invalidValue() {
		AnnotationBuilder<Single> builder = AnnotationBuilder.of(Single.class);

		AnnotationBuilder.MemberExtractor<Single, Object> extractor = Single::value;

		Object fakeValue = AnnotationBuilderTest.class;

		assertThatThrownBy(() -> builder.with(extractor, fakeValue))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Value " + fakeValue + " (" + fakeValue.getClass() + ")"
				+ " cannot be provided for member value of type class java.lang.String");
	}

	@Nested
	class OfMarker {
		private final Marker annotation = AnnotationBuilder.marker(Marker.class);

		@Test
		void isInstanceOf() {
			assertThat(annotation)
				.isInstanceOf(Marker.class);
		}

		@Test
		void isEquals() {
			assertThat(annotation)
				.isNotEqualTo(new Object())
				.isNotEqualTo(null)
				.isNotEqualTo(1)
				.isEqualTo(annotation)
				.isEqualTo(MarkerHolder.HELD)
				.isNotEqualTo(SingleHolder.HELD)
				.isNotEqualTo(MultipleHolder.HELD);
			assertThat(MarkerHolder.HELD)
				.isEqualTo(annotation);
		}

		@Test
		void hasAnnotationType() {
			assertThat(annotation)
				.returns(Marker.class, Annotation::annotationType);
		}

		@Test
		void hasHashCode() {
			assertThat(annotation)
				.returns(0, Object::hashCode)
				.returns(MarkerHolder.HELD.hashCode(), Object::hashCode);
		}

		@Test
		void hasRepresentation() {
			String expected = String.format("@%s$Marker()", AnnotationBuilderTest.class.getName());
			assertThat(annotation)
				.returns(expected, Object::toString)
				.returns(MarkerHolder.HELD.toString(), Object::toString);
		}
	}

	@Test
	void testSingleMissingValue() {
		AnnotationBuilder<Single> builder = AnnotationBuilder.of(Single.class);

		assertThatThrownBy(() -> builder.build())
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("No value set for member 'value'");
	}

	@Nested
	class OfSingle {
		private final Single annotation = AnnotationBuilder.of(Single.class)
			.with(Single::value, SingleHolder.VALUE)
			.build();

		@Test
		void isInstanceOf() {
			assertThat(annotation)
				.isInstanceOf(Single.class);
		}

		@Test
		void isEquals() {
			assertThat(annotation)
				.isNotEqualTo(new Object())
				.isNotEqualTo(null)
				.isNotEqualTo(1)
				.isEqualTo(annotation)
				.isEqualTo(SingleHolder.HELD);
			assertThat(SingleHolder.HELD)
				.isEqualTo(annotation);
		}

		@Test
		void hasAnnotationType() {
			assertThat(annotation)
				.returns(Single.class, Annotation::annotationType);
		}

		@Test
		void hasHashCode() {
			int expectedHashCode = 127 * "value".hashCode() ^ SingleHolder.VALUE.hashCode();

			assertThat(annotation)
				.returns(expectedHashCode, Object::hashCode)
				.returns(SingleHolder.HELD.hashCode(), Object::hashCode);
		}

		@Test
		void hasRepresentation() {
			String expected = String.format("@%s$Single(value=\"testValue\")", AnnotationBuilderTest.class.getName());
			assertThat(annotation)
				.returns(expected, Object::toString);
		}
	}

	@Nested
	class OfMultiple {
		@Nested
		class Matching {
			private static final String TWO_VALUE = MultipleHolder.TWO_VALUE;
			private static final int THREE_VALUE = MultipleHolder.TRHEE_VALUE;
			private final Multiple annotation = AnnotationBuilder.of(Multiple.class)
				.with(Multiple::one, Multiple.DEFAULT_ONE)
				.with(Multiple::two, TWO_VALUE)
				.with(Multiple::three, THREE_VALUE)
				.build();

			@Test
			void isInstanceOf() {
				assertThat(annotation)
					.isInstanceOf(Multiple.class);
			}

			@Test
			void isEquals() {
				assertThat(annotation)
					.isNotEqualTo(new Object())
					.isNotEqualTo(null)
					.isNotEqualTo(1)
					.isEqualTo(annotation)
					.isNotEqualTo(MarkerHolder.HELD)
					.isNotEqualTo(SingleHolder.HELD)
					.isEqualTo(MultipleHolder.HELD);
				assertThat(MultipleHolder.HELD)
					.isEqualTo(annotation);
			}

			@Test
			void hasAnnotationType() {
				assertThat(annotation)
					.returns(Multiple.class, Annotation::annotationType);
			}

			@Test
			void hasHashCode() {
				int expectedHashCode = (127 * "one".hashCode() ^ Multiple.DEFAULT_ONE.hashCode())
					+ (127 * "two".hashCode() ^ TWO_VALUE.hashCode())
					+ (127 * "three".hashCode() ^ Integer.valueOf(THREE_VALUE).hashCode());

				assertThat(annotation)
					.returns(expectedHashCode, Object::hashCode)
					.returns(MultipleHolder.HELD.hashCode(), Object::hashCode);
			}

			@Test
			void hasRepresentation() {
				String expected = String.format("@%s$Multiple(one=\"defaultOne\", two=\"testValue\", three=3)",
					AnnotationBuilderTest.class.getName());
				assertThat(annotation)
					.returns(MultipleHolder.HELD.toString(), Object::toString)
					.returns(expected, Object::toString);
			}
		}

		@Nested
		class Mismatching {
			private static final String TWO_VALUE = "twoOther";
			private static final int THREE_VALUE = 100;
			private final Multiple annotation = AnnotationBuilder.of(Multiple.class)
				.with(Multiple::two, TWO_VALUE)
				.with(Multiple::three, THREE_VALUE)
				.build();

			@Test
			void isInstanceOf() {
				assertThat(annotation)
					.isInstanceOf(Multiple.class);
			}

			@Test
			void isEquals() {
				assertThat(annotation)
					.isNotEqualTo(new Object())
					.isNotEqualTo(null)
					.isNotEqualTo(1)
					.isEqualTo(annotation)
					.isNotEqualTo(MarkerHolder.HELD)
					.isNotEqualTo(SingleHolder.HELD)
					.isNotEqualTo(MultipleHolder.HELD)
					.isNotEqualTo(ArrayElementsHolder.HELD);
				assertThat(MarkerHolder.HELD)
					.isNotEqualTo(annotation);
				assertThat(SingleHolder.HELD)
					.isNotEqualTo(annotation);
				assertThat(MultipleHolder.HELD)
					.isNotEqualTo(annotation);
				assertThat(ArrayElementsHolder.HELD)
					.isNotEqualTo(annotation);
			}

			@Test
			void hasAnnotationType() {
				assertThat(annotation)
					.returns(Multiple.class, Annotation::annotationType);
			}

			@Test
			void hasHashCode() {
				int expectedHashCode = (127 * "one".hashCode() ^ Multiple.DEFAULT_ONE.hashCode())
					+ (127 * "two".hashCode() ^ TWO_VALUE.hashCode())
					+ (127 * "three".hashCode() ^ Integer.valueOf(THREE_VALUE).hashCode());

				assertThat(annotation)
					.returns(expectedHashCode, Object::hashCode);
			}


			@Test
			void hasRepresentation() {
				String expected = String.format("@%s$Multiple(one=\"defaultOne\", two=\"twoOther\", three=100)",
					AnnotationBuilderTest.class.getName());
				assertThat(annotation)
					.returns(expected, Object::toString);
			}
		}

	}

	@Nested
	class OfArrayElements {
		private final ArrayElements annotation = AnnotationBuilder.of(ArrayElements.class)
			.with(ArrayElements::value, new String[] {"one", "two", "three"})
			.build();

		@Test
		void isInstanceOf() {
			assertThat(annotation)
				.isInstanceOf(ArrayElements.class);
		}

		@Test
		void isEquals() {
			assertThat(annotation)
				.isNotEqualTo(new Object())
				.isNotEqualTo(null)
				.isNotEqualTo(1)
				.isEqualTo(annotation)
				.isEqualTo(ArrayElementsHolder.HELD);
			assertThat(ArrayElementsHolder.HELD)
				.isEqualTo(annotation);
		}

		@Test
		void hasAnnotationType() {
			assertThat(annotation)
				.returns(ArrayElements.class, Annotation::annotationType);
		}

		@Test
		void hasHashCode() {
			int expectedHashCode = 127 * "value".hashCode() ^ Arrays.hashCode(ArrayElementsHolder.VALUE);

			assertThat(annotation)
				.returns(expectedHashCode, Object::hashCode)
				.returns(ArrayElementsHolder.HELD.hashCode(), Object::hashCode);
		}

		@Test
		void hasRepresentation() {
			String expected = String.format("@%s$ArrayElements(value={\"one\", \"two\", \"three\"})",
				AnnotationBuilderTest.class.getName());
			assertThat(annotation)
				.returns(expected, Object::toString);
		}
	}


	@Retention(RetentionPolicy.RUNTIME)
	@interface Marker {

	}

	@Marker
	private static class MarkerHolder {
		@SuppressWarnings("assignment.type.incompatible")
		static final Marker HELD = MarkerHolder.class.getAnnotation(Marker.class);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface Single {
		String value();
	}

	@Single(SingleHolder.VALUE)
	private static class SingleHolder {
		static final String VALUE = "testValue";
		@SuppressWarnings("assignment.type.incompatible")
		static final Single HELD = SingleHolder.class.getAnnotation(Single.class);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface Multiple {

		String DEFAULT_ONE = "defaultOne";

		String one() default DEFAULT_ONE;

		String two();

		int three();
	}

	@Multiple(two = MultipleHolder.TWO_VALUE, three = MultipleHolder.TRHEE_VALUE)
	private static class MultipleHolder {
		static final String TWO_VALUE = "testValue";
		static final int TRHEE_VALUE = 3;
		@SuppressWarnings("assignment.type.incompatible")
		static final Multiple HELD = MultipleHolder.class.getAnnotation(Multiple.class);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface ArrayElements {
		String[] value() default {};
	}

	@ArrayElements({"one", "two", "three"})
	private static class ArrayElementsHolder {
		static final String[] VALUE = new String[] {"one", "two", "three"};
		@SuppressWarnings("assignment.type.incompatible")
		static final ArrayElements HELD = ArrayElementsHolder.class.getAnnotation(ArrayElements.class);
	}

}
