package org.perfectable.introspection.query;

import org.perfectable.introspection.Subject;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.AbstractQueryAssert.assertThat;

class AnnotationQueryTest {
	@Test
	void testEmpty() {
		AnnotationQuery<?> query = AnnotationQuery.empty();

		assertThat(query)
			.<Class<? extends Annotation>>extracting(Annotation::annotationType)
			.isEmpty()
			.doesNotContain(String.class, Subject.Special.class, Subject.OtherAnnotation.class);
	}

	@Test
	void testEmptyFilter() {
		AnnotationQuery<?> query = AnnotationQuery.empty()
			.filter(annotation -> annotation.toString().equals("None"));

		assertThat(query)
			.<Class<? extends Annotation>>extracting(Annotation::annotationType)
			.isEmpty()
			.doesNotContain(String.class, Subject.Special.class, Subject.OtherAnnotation.class);
	}

	@Test
	void testString() {
		AnnotationQuery<?> query = AnnotationQuery.of(String.class);

		assertThat(query)
			.<Class<? extends Annotation>>extracting(Annotation::annotationType)
			.isEmpty()
			.doesNotContain(String.class, Subject.Special.class, Subject.OtherAnnotation.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSubjectUnrestricted() {
		AnnotationQuery<Annotation> query = AnnotationQuery.of(Subject.class);

		assertThat(query)
			.<Class<? extends Annotation>>extracting(Annotation::annotationType)
			.contains(Subject.Special.class, Subject.OtherAnnotation.class)
			.doesNotContain(String.class, Nullable.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSubjectTyped() {
		AnnotationQuery<?> query = AnnotationQuery.of(Subject.class)
			.typed(Subject.Special.class);

		assertThat(query)
			.<Class<? extends Annotation>>extracting(Annotation::annotationType)
			.isSingleton(Subject.Special.class)
			.doesNotContain(String.class, Subject.OtherAnnotation.class, Nullable.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testPredicated() {
		AnnotationQuery<?> query = AnnotationQuery.of(Subject.class)
			.filter(annotation -> annotation.annotationType().getSimpleName().charAt(0) == 'O');

		assertThat(query)
			.<Class<? extends Annotation>>extracting(Annotation::annotationType)
			.isSingleton(Subject.OtherAnnotation.class)
			.doesNotContain(String.class, Subject.Special.class, Nullable.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testAnnotatedWith() {
		AnnotationQuery<Annotation> query = AnnotationQuery.of(Subject.class)
			.annotatedWith(Retention.class);

		assertThat(query)
			.<Class<? extends Annotation>>extracting(Annotation::annotationType)
			.contains(Subject.Special.class, Subject.OtherAnnotation.class)
			.doesNotContain(String.class, Nullable.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testJoin() {
		AnnotationQuery<Subject.Special> first =
			AnnotationQuery.of(Subject.class).typed(Subject.Special.class);
		AnnotationQuery<Documented> second =
			AnnotationQuery.of(Retention.class).typed(Documented.class);
		AnnotationQuery<Annotation> query = first.join(second);

		assertThat(query)
			.<Class<? extends Annotation>>extracting(Annotation::annotationType)
			.contains(Subject.Special.class, Documented.class)
			.doesNotContain(String.class, Subject.OtherAnnotation.class, Retention.class, Nullable.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testJoinMultiple() {
		AnnotationQuery<Annotation> query = AnnotationQuery.empty()
			.join(AnnotationQuery.of(Retention.class).typed(Documented.class))
			.join(AnnotationQuery.of(Subject.class).typed(Subject.Special.class))
			.join(AnnotationQuery.of(Documented.class).typed(Documented.class));

		assertThat(query)
			.<Class<? extends Annotation>>extracting(Annotation::annotationType)
			.contains(Documented.class, Subject.Special.class, Documented.class)
			.doesNotContain(String.class, Subject.OtherAnnotation.class, Retention.class, Nullable.class);
	}

}
