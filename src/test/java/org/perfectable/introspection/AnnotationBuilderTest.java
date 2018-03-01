package org.perfectable.introspection;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// SUPPRESS FILE MagicNumber
// SUPPRESS FILE MultipleStringLiterals
class AnnotationBuilderTest {

	@Test
	void testMarker() {
		Marker annotation = AnnotationBuilder.marker(Marker.class);

		int expectedHashCode = 0;
		assertThat(annotation)
			.isInstanceOf(Marker.class)
			.isNotEqualTo(new Object())
			.isNotEqualTo(null)
			.isNotEqualTo(1)
			.isEqualTo(annotation)
			.isEqualTo(MarkerHolder.HELD)
			.isNotEqualTo(SingleHolder.HELD)
			.isNotEqualTo(MultipleHolder.HELD)
			.returns(Marker.class, Annotation::annotationType)
			.returns(expectedHashCode, Object::hashCode)
			.returns(MarkerHolder.HELD.hashCode(), Object::hashCode)
			.returns("@org.perfectable.introspection.AnnotationBuilderTest$Marker()", Object::toString)
			.returns(MarkerHolder.HELD.toString(), Object::toString);
	}

	@Test
	void testSingleMissingValue() {
		AnnotationBuilder<Single> builder = AnnotationBuilder.of(Single.class);

		assertThatThrownBy(() -> builder.build())
			.isInstanceOf(IllegalStateException.class)
			.hasMessage("No value set for member 'value'");
	}

	@Test
	void testSingleWithValue() {
		Single annotation = AnnotationBuilder.of(Single.class)
			.with(Single::value, SingleHolder.VALUE)
			.build();

		String expectedToString = "@org.perfectable.introspection.AnnotationBuilderTest$Single(value=testValue)";
		int expectedHashCode = 127 * "value".hashCode() ^ SingleHolder.VALUE.hashCode();
		assertThat(annotation)
			.isInstanceOf(Single.class)
			.isNotEqualTo(new Object())
			.isNotEqualTo(null)
			.isNotEqualTo(1)
			.isEqualTo(annotation)
			.isEqualTo(SingleHolder.HELD)
			.returns(Single.class, Annotation::annotationType)
			.returns(expectedHashCode, Object::hashCode)
			.returns(SingleHolder.HELD.hashCode(), Object::hashCode)
			.returns(expectedToString, Object::toString)
			.returns(SingleHolder.HELD.toString(), Object::toString);
	}

	@Test
	void testMultipleMisMatching() {
		String twoValue = "twoOther";
		int threeValue = 100;
		Multiple annotation = AnnotationBuilder.of(Multiple.class)
			.with(Multiple::two, twoValue)
			.with(Multiple::three, threeValue)
			.build();

		int expectedHashCode = (127 * "one".hashCode() ^ Multiple.DEFAULT_ONE.hashCode())
			+ (127 * "two".hashCode() ^ twoValue.hashCode())
			+ (127 * "three".hashCode() ^ Integer.valueOf(threeValue).hashCode());
		String expectedToString =
			"@org.perfectable.introspection.AnnotationBuilderTest$Multiple(two=twoOther, three=100)";
		assertThat(annotation)
			.isInstanceOf(Multiple.class)
			.isNotEqualTo(new Object())
			.isNotEqualTo(null)
			.isNotEqualTo(1)
			.isEqualTo(annotation)
			.isNotEqualTo(MarkerHolder.HELD)
			.isNotEqualTo(SingleHolder.HELD)
			.isNotEqualTo(MultipleHolder.HELD)
			.returns(Multiple.class, Annotation::annotationType)
			.returns(expectedHashCode, Object::hashCode)
			.returns(expectedToString, Object::toString);
	}


	@Test
	void testMultipleMatching() {
		String twoValue = MultipleHolder.TWO_VALUE;
		int threeValue = MultipleHolder.TRHEE_VALUE;
		Multiple annotation = AnnotationBuilder.of(Multiple.class)
			.with(Multiple::one, Multiple.DEFAULT_ONE)
			.with(Multiple::two, twoValue)
			.with(Multiple::three, threeValue)
			.build();

		int expectedHashCode = (127 * "one".hashCode() ^ Multiple.DEFAULT_ONE.hashCode())
			+ (127 * "two".hashCode() ^ twoValue.hashCode())
			+ (127 * "three".hashCode() ^ Integer.valueOf(threeValue).hashCode());
		String expectedToString =
			"@org.perfectable.introspection.AnnotationBuilderTest$Multiple(one=defaultOne, two=testValue, three=3)";
		assertThat(annotation)
			.isInstanceOf(Multiple.class)
			.isNotEqualTo(new Object())
			.isNotEqualTo(null)
			.isNotEqualTo(1)
			.isEqualTo(annotation)
			.isNotEqualTo(MarkerHolder.HELD)
			.isNotEqualTo(SingleHolder.HELD)
			.isEqualTo(MultipleHolder.HELD)
			.returns(Multiple.class, Annotation::annotationType)
			.returns(expectedHashCode, Object::hashCode)
			.returns(MultipleHolder.HELD.hashCode(), Object::hashCode)
			.returns(expectedToString, Object::toString)
			.returns(MultipleHolder.HELD.hashCode(), Object::hashCode);
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
