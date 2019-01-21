package org.perfectable.introspection; // SUPPRESS LENGTH

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// SUPPRESS FILE MagicNumber
// SUPPRESS FILE MultipleStringLiterals
@SuppressWarnings("ClassCanBeStatic")
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

	@Test
	void nullValue() {
		AnnotationBuilder<Single> builder = AnnotationBuilder.of(Single.class);

		assertThatThrownBy(() -> builder.with(Single::value, null))
			.isInstanceOf(NullPointerException.class);
	}

	@Test
	void invalidValue() {
		AnnotationBuilder<Single> builder = AnnotationBuilder.of(Single.class);

		AnnotationBuilder.MemberExtractor<Single, Object> extractor = Single::value;

		Object fakeValue = AnnotationBuilderTest.class;

		assertThatThrownBy(() -> builder.with(extractor, fakeValue))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Value " + fakeValue + " cannot be provided for member value of type class java.lang.String");
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
					.isNotEqualTo(MultipleHolder.HELD);
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
		}

	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface Marker {

	}

	@Marker
	private static class MarkerHolder {
		static final Marker HELD = MarkerHolder.class.getAnnotation(Marker.class);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface Single {
		String value();
	}

	@Single(SingleHolder.VALUE)
	private static class SingleHolder {
		static final String VALUE = "testValue";
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
		static final Multiple HELD = MultipleHolder.class.getAnnotation(Multiple.class);
	}
}
