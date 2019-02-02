package org.perfectable.introspection.query; // SUPPRESS LENGTH

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.SimpleReflections.getMethod;
import static org.perfectable.introspection.query.AnnotationFilterAssert.assertThat;

class AnnotationFilterTest {

	private static final Method NO_ANNOTATION_METHOD = getMethod(AnnotationFilterTest.class, "noAnnotation");
	private static final Method FIRST_WITH_TRUE_METHOD = getMethod(AnnotationFilterTest.class, "firstWithTrue");

	@Test
	void accepting() {
		AnnotationFilter filter = AnnotationFilter.ACCEPTING;

		assertThat(filter)
			.matchesElement(Number.class)
			.matchesElement(String.class)
			.matchesElement(NO_ANNOTATION_METHOD)
			.matchesElement(First.class)
			.matchesElement(FirstPresent.class)
			.matchesElement(NoAnnotation.class);
	}

	@Test
	void rejecting() {
		AnnotationFilter filter = AnnotationFilter.REJECTING;

		assertThat(filter)
			.doesntMatchElement(Number.class)
			.doesntMatchElement(String.class)
			.doesntMatchElement(NO_ANNOTATION_METHOD)
			.doesntMatchElement(First.class)
			.doesntMatchElement(FirstPresent.class)
			.doesntMatchElement(NoAnnotation.class);
	}

	@Test
	void absent() {
		AnnotationFilter filter = AnnotationFilter.ABSENT;

		assertThat(filter)
			.matchesElement(NO_ANNOTATION_METHOD)
			.doesntMatchElement(FIRST_WITH_TRUE_METHOD)
			.doesntMatchElement(First.class) // Retention counts
			.doesntMatchElement(Second.class)
			.matchesElement(NoAnnotation.class)
			.doesntMatchElement(FirstPresent.class)
			.doesntMatchElement(SecondPresent.class)
			.doesntMatchElement(BothPresent.class)
			.doesntMatchElement(SecondWithTruePresent.class);
	}

	@Test
	void single() {
		AnnotationFilter filter = AnnotationFilter.single(First.class);

		assertThat(filter)
			.doesntMatchElement(NO_ANNOTATION_METHOD)
			.matchesElement(FIRST_WITH_TRUE_METHOD)
			.doesntMatchElement(First.class)
			.matchesElement(Second.class)
			.doesntMatchElement(NoAnnotation.class)
			.matchesElement(FirstPresent.class)
			.matchesElement(BothPresent.class)
			.doesntMatchElement(SecondWithTruePresent.class);
	}

	@Test
	void singleWithTest() {
		AnnotationFilter filter = AnnotationFilter.single(First.class).andMatching(First::value);

		assertThat(filter)
			.doesntMatchElement(NO_ANNOTATION_METHOD)
			.matchesElement(FIRST_WITH_TRUE_METHOD)
			.doesntMatchElement(First.class)
			.doesntMatchElement(Second.class)
			.doesntMatchElement(NoAnnotation.class)
			.doesntMatchElement(FirstPresent.class)
			.doesntMatchElement(SecondWithTruePresent.class);
	}

	@Test
	void alternative() {
		AnnotationFilter filter = AnnotationFilter.single(First.class).or(AnnotationFilter.single(Second.class));

		assertThat(filter)
			.doesntMatchElement(NO_ANNOTATION_METHOD)
			.matchesElement(FIRST_WITH_TRUE_METHOD)
			.doesntMatchElement(First.class)
			.matchesElement(Second.class)
			.doesntMatchElement(NoAnnotation.class)
			.matchesElement(FirstPresent.class)
			.matchesElement(BothPresent.class)
			.matchesElement(SecondWithTruePresent.class);
	}

	@Test
	void conjunction() {
		AnnotationFilter filter = AnnotationFilter.single(First.class).and(AnnotationFilter.single(Second.class));

		assertThat(filter)
			.doesntMatchElement(NO_ANNOTATION_METHOD)
			.doesntMatchElement(FIRST_WITH_TRUE_METHOD)
			.doesntMatchElement(First.class)
			.doesntMatchElement(Second.class)
			.doesntMatchElement(NoAnnotation.class)
			.doesntMatchElement(FirstPresent.class)
			.matchesElement(BothPresent.class)
			.doesntMatchElement(SecondWithTruePresent.class);
	}

	@Test
	void conjunctionMixed() {
		AnnotationFilter filter = AnnotationFilter.single(First.class).negated()
			.and(AnnotationFilter.single(Second.class).negated());

		assertThat(filter)
			.matchesElement(NO_ANNOTATION_METHOD)
			.doesntMatchElement(FIRST_WITH_TRUE_METHOD)
			.matchesElement(First.class)
			.doesntMatchElement(Second.class)
			.matchesElement(NoAnnotation.class)
			.doesntMatchElement(FirstPresent.class)
			.doesntMatchElement(BothPresent.class)
			.doesntMatchElement(SecondWithTruePresent.class);
	}

	@Test
	void custom() {
		AnnotationFilter filter = First.class::equals;

		assertThat(filter)
			.doesntMatchElement(NO_ANNOTATION_METHOD)
			.matchesElement(First.class)
			.doesntMatchElement(Second.class)
			.doesntMatchElement(FirstPresent.class)
			.doesntMatchElement(NoAnnotation.class)
			.doesntMatchElement(SecondWithTruePresent.class);
	}


	@Test
	void customNegated() {
		AnnotationFilter filter = ((AnnotationFilter) First.class::equals).negated();

		assertThat(filter)
			.matchesElement(NO_ANNOTATION_METHOD)
			.doesntMatchElement(First.class)
			.matchesElement(Second.class)
			.matchesElement(FirstPresent.class)
			.matchesElement(NoAnnotation.class)
			.matchesElement(SecondWithTruePresent.class);
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface First {
		boolean value() default false;
	}

	@First
	@Retention(RetentionPolicy.RUNTIME)
	@interface Second {
		boolean value() default false;
	}

	@SuppressWarnings("unused")
	private static void noAnnotation() {
		// tests only
	}

	@SuppressWarnings("unused")
	@First(true)
	private static void firstWithTrue() {
		// tests only
	}

	private static final class NoAnnotation {
		// tests only
	}

	@First
	private static final class FirstPresent {
		// tests only
	}

	@Second
	private static final class SecondPresent {
		// tests only
	}

	@Second(true)
	private static final class SecondWithTruePresent {
		// tests only
	}

	@First
	@Second
	private static final class BothPresent {
		// tests only
	}
}
