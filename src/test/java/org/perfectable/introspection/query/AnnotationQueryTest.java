package org.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.AbstractQueryAssert.assertThat;

class AnnotationQueryTest {
	private static final String EXAMPLE_STRING = "testString";

	private static final Subject.Special INSTANCE_SPECIAL =
		Subject.class.getAnnotation(Subject.Special.class);
	private static final Subject.OtherAnnotation INSTANCE_OTHER =
		Subject.class.getAnnotation(Subject.OtherAnnotation.class);
	private static final Nullable INSTANCE_NULLABLE =
		SubjectReflection.ANNOTATED_WITH_NULLABLE.getAnnotation(Nullable.class);
	private static final Documented INSTANCE_DOCUMENTED =
		Nullable.class.getAnnotation(Documented.class);
	private static final Retention INSTANCE_RETENTION =
		Nullable.class.getAnnotation(Retention.class);

	@Test
	void testEmpty() {
		AnnotationQuery<?> query = AnnotationQuery.empty();

		assertThat(query)
			.isEmpty()
			.doesNotContain(EXAMPLE_STRING, INSTANCE_SPECIAL, INSTANCE_OTHER);
	}

	@Test
	void testEmptyFilter() {
		AnnotationQuery<?> query = AnnotationQuery.empty()
			.filter(annotation -> annotation.toString().equals("None"));

		assertThat(query)
			.isEmpty()
			.doesNotContain(EXAMPLE_STRING, INSTANCE_SPECIAL, INSTANCE_OTHER);
	}

	@Test
	void testString() {
		AnnotationQuery<?> query = AnnotationQuery.of(String.class);

		assertThat(query)
			.isEmpty()
			.doesNotContain(EXAMPLE_STRING, INSTANCE_SPECIAL, INSTANCE_OTHER);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSubjectUnrestricted() {
		AnnotationQuery<Annotation> query = AnnotationQuery.of(Subject.class);

		assertThat(query)
			.contains(INSTANCE_SPECIAL, INSTANCE_OTHER)
			.doesNotContain(EXAMPLE_STRING, INSTANCE_NULLABLE);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testSubjectTyped() {
		AnnotationQuery<?> query = AnnotationQuery.of(Subject.class)
			.typed(Subject.Special.class);

		AbstractQueryAssert.<Annotation>assertThat(query)
			.isSingleton(INSTANCE_SPECIAL)
			.doesNotContain(EXAMPLE_STRING, INSTANCE_OTHER, INSTANCE_NULLABLE);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testPredicated() {
		AnnotationQuery<?> query = AnnotationQuery.of(Subject.class)
			.filter(annotation -> annotation.annotationType().getSimpleName().charAt(0) == 'O');

		AbstractQueryAssert.<Annotation>assertThat(query)
			.isSingleton(INSTANCE_OTHER)
			.doesNotContain(EXAMPLE_STRING, INSTANCE_SPECIAL, INSTANCE_NULLABLE);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testAnnotatedWith() {
		AnnotationQuery<Annotation> query = AnnotationQuery.of(Subject.class)
			.annotatedWith(Retention.class);

		assertThat(query)
			.contains(INSTANCE_SPECIAL, INSTANCE_OTHER)
			.doesNotContain(EXAMPLE_STRING, INSTANCE_NULLABLE);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testFromElementsUnrestricted() {
		AnnotationQuery<Annotation> query =
			AnnotationQuery.fromElements(INSTANCE_DOCUMENTED, INSTANCE_RETENTION, INSTANCE_SPECIAL)
				.annotatedWith(Target.class);

		assertThat(query)
			.contains(INSTANCE_DOCUMENTED, INSTANCE_RETENTION)
			.doesNotContain(INSTANCE_SPECIAL);
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
			.contains(INSTANCE_SPECIAL, INSTANCE_DOCUMENTED)
			.doesNotContain(EXAMPLE_STRING, INSTANCE_OTHER, INSTANCE_RETENTION,
				INSTANCE_NULLABLE);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testJoinMultiple() {
		AnnotationQuery<Annotation> query = AnnotationQuery.empty()
			.join(AnnotationQuery.of(Retention.class).typed(Documented.class))
			.join(AnnotationQuery.of(Subject.class).typed(Subject.Special.class))
			.join(AnnotationQuery.of(Documented.class).typed(Documented.class));

		assertThat(query)
			.contains(INSTANCE_DOCUMENTED, INSTANCE_SPECIAL, INSTANCE_DOCUMENTED)
			.doesNotContain(EXAMPLE_STRING, INSTANCE_OTHER, INSTANCE_RETENTION,
				INSTANCE_NULLABLE);
	}


}
