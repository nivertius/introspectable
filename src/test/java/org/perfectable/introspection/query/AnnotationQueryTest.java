package org.perfectable.introspection.query;

import org.perfectable.introspection.Subject;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import org.assertj.core.api.iterable.Extractor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnnotationQueryTest {

	private static class Root {
		// test class
	}

	private static class Branch extends Root {
		// test class
	}

	private static class Leaf extends Branch {
		// test class
	}

	@Test
	void testEmpty() {
		AnnotationQuery<?> chain = AnnotationQuery.empty();

		assertThat(chain)
			.containsExactlyInAnyOrder();
	}

	@Test
	void testEmptyFilter() {
		AnnotationQuery<?> chain = AnnotationQuery.empty()
			.filter(annotation -> annotation.toString().equals("None"));

		assertThat(chain)
			.containsExactlyInAnyOrder();
	}

	@Test
	void testString() {
		AnnotationQuery<?> chain = AnnotationQuery.of(String.class);

		assertThat(chain)
			.containsExactlyInAnyOrder();
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSubjectUnrestricted() {
		AnnotationQuery<Annotation> chain = AnnotationQuery.of(Subject.class);

		assertThat(chain)
			.extracting((Extractor<Annotation, Class<? extends Annotation>>) Annotation::annotationType)
			.containsExactlyInAnyOrder(Subject.Special.class, Subject.OtherAnnotation.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSubjectTyped() {
		AnnotationQuery<?> chain = AnnotationQuery.of(Subject.class)
			.typed(Subject.Special.class);

		assertThat(chain)
			.extracting((Extractor<Annotation, Class<? extends Annotation>>) Annotation::annotationType)
			.containsExactlyInAnyOrder(Subject.Special.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testPredicated() {
		AnnotationQuery<?> chain = AnnotationQuery.of(Subject.class)
			.filter(annotation -> annotation.annotationType().getSimpleName().charAt(0) == 'O');

		assertThat(chain)
			.extracting((Extractor<Annotation, Class<? extends Annotation>>) Annotation::annotationType)
			.containsExactlyInAnyOrder(Subject.OtherAnnotation.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testAnnotatedWith() {
		AnnotationQuery<Annotation> chain = AnnotationQuery.empty()
			.join(AnnotationQuery.of(Subject.class).annotatedWith(Retention.class));

		assertThat(chain)
			.extracting((Extractor<Annotation, Class<? extends Annotation>>) Annotation::annotationType)
			.containsExactlyInAnyOrder(Subject.Special.class, Subject.OtherAnnotation.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testJoin() {
		AnnotationQuery<Subject.Special> first =
			AnnotationQuery.of(Subject.class).typed(Subject.Special.class);
		AnnotationQuery<Documented> second =
			AnnotationQuery.of(Retention.class).typed(Documented.class);
		AnnotationQuery<Annotation> chain = first.join(second);

		assertThat(chain)
			.extracting((Extractor<Annotation, Class<? extends Annotation>>) Annotation::annotationType)
			.containsExactlyInAnyOrder(Subject.Special.class, Documented.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testJoinMultiple() {
		AnnotationQuery<Annotation> chain = AnnotationQuery.empty()
			.join(AnnotationQuery.of(Retention.class).typed(Documented.class))
			.join(AnnotationQuery.of(Subject.class).typed(Subject.Special.class))
			.join(AnnotationQuery.of(Documented.class).typed(Documented.class));

		assertThat(chain)
			.extracting((Extractor<Annotation, Class<? extends Annotation>>) Annotation::annotationType)
			.containsExactlyInAnyOrder(Subject.Special.class, Documented.class, Documented.class);
	}

}
