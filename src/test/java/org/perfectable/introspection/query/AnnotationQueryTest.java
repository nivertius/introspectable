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

	private static final Documented INSTANCE_DOCUMENTED =
		Nullable.class.getAnnotation(Documented.class);
	private static final Retention INSTANCE_RETENTION =
		Nullable.class.getAnnotation(Retention.class);

	@Test
	void testEmpty() {
		AnnotationQuery<?> query = AnnotationQuery.empty();

		assertThat(query)
			.isEmpty()
			.doesNotContain(EXAMPLE_STRING, SubjectReflection.INSTANCE_SPECIAL, SubjectReflection.INSTANCE_OTHER);
	}

	@Test
	void testEmptyFilter() {
		AnnotationQuery<?> query = AnnotationQuery.empty()
			.filter(annotation -> annotation.toString().equals("None"));

		assertThat(query)
			.isEmpty()
			.doesNotContain(EXAMPLE_STRING, SubjectReflection.INSTANCE_SPECIAL, SubjectReflection.INSTANCE_OTHER);
	}

	@Test
	void testString() {
		AnnotationQuery<?> query = AnnotationQuery.of(String.class);

		assertThat(query)
			.isEmpty()
			.doesNotContain(EXAMPLE_STRING, SubjectReflection.INSTANCE_SPECIAL, SubjectReflection.INSTANCE_OTHER);
	}

	@Test
	void testSubjectUnrestricted() {
		AnnotationQuery<Annotation> query = AnnotationQuery.of(Subject.class);

		assertThat(query)
			.contains(SubjectReflection.INSTANCE_SPECIAL, SubjectReflection.INSTANCE_OTHER,
				SubjectReflection.REPETITION_CONTAINER)
			.doesNotContain(EXAMPLE_STRING, SubjectReflection.INSTANCE_NULLABLE, SubjectReflection.REPETITIONS[0]);
	}

	@Test
	void testSubjectTyped() {
		AnnotationQuery<?> query = AnnotationQuery.of(Subject.class)
			.typed(Subject.Special.class);

		AbstractQueryAssert.<Annotation>assertThat(query)
			.isSingleton(SubjectReflection.INSTANCE_SPECIAL)
			.doesNotContain(EXAMPLE_STRING, SubjectReflection.INSTANCE_OTHER, SubjectReflection.INSTANCE_NULLABLE);
	}

	@Test
	void testPredicated() {
		AnnotationQuery<?> query = AnnotationQuery.of(Subject.class)
			.filter(annotation -> annotation.annotationType().getSimpleName().charAt(0) == 'O');

		AbstractQueryAssert.<Annotation>assertThat(query)
			.isSingleton(SubjectReflection.INSTANCE_OTHER)
			.doesNotContain(EXAMPLE_STRING, SubjectReflection.INSTANCE_SPECIAL, SubjectReflection.INSTANCE_NULLABLE);
	}

	@Test
	void testAnnotatedWith() {
		AnnotationQuery<Annotation> query = AnnotationQuery.of(Subject.class)
			.annotatedWith(Retention.class);

		assertThat(query)
			.contains(SubjectReflection.INSTANCE_SPECIAL, SubjectReflection.INSTANCE_OTHER,
				SubjectReflection.REPETITION_CONTAINER)
			.doesNotContain(EXAMPLE_STRING, SubjectReflection.INSTANCE_NULLABLE);
	}

	@Test
	void testRepeatableUnroll() {
		AnnotationQuery<Annotation> query = AnnotationQuery.of(Subject.class)
			.withRepeatableUnroll();

		assertThat(query)
			.contains(SubjectReflection.INSTANCE_SPECIAL, SubjectReflection.INSTANCE_OTHER,
				SubjectReflection.REPETITION_CONTAINER,
				SubjectReflection.REPETITIONS[0], SubjectReflection.REPETITIONS[1])
			.doesNotContain(EXAMPLE_STRING);
	}

	@Test
	void testFromElementsUnrestricted() {
		AnnotationQuery<Annotation> query =
			AnnotationQuery.fromElements(INSTANCE_DOCUMENTED, INSTANCE_RETENTION, SubjectReflection.INSTANCE_SPECIAL)
				.annotatedWith(Target.class);

		assertThat(query)
			.contains(INSTANCE_DOCUMENTED, INSTANCE_RETENTION)
			.doesNotContain(SubjectReflection.INSTANCE_SPECIAL);
	}

	@Test
	void testJoin() {
		AnnotationQuery<Subject.Special> first =
			AnnotationQuery.of(Subject.class).typed(Subject.Special.class);
		AnnotationQuery<Documented> second =
			AnnotationQuery.of(Retention.class).typed(Documented.class);
		AnnotationQuery<Annotation> query = first.join(second);

		assertThat(query)
			.contains(SubjectReflection.INSTANCE_SPECIAL, INSTANCE_DOCUMENTED)
			.doesNotContain(EXAMPLE_STRING, SubjectReflection.INSTANCE_OTHER, INSTANCE_RETENTION,
				SubjectReflection.INSTANCE_NULLABLE);
	}

	@Test
	void testJoinMultiple() {
		AnnotationQuery<Annotation> query = AnnotationQuery.empty()
			.join(AnnotationQuery.of(Retention.class).typed(Documented.class))
			.join(AnnotationQuery.of(Subject.class).typed(Subject.Special.class))
			.join(AnnotationQuery.of(Documented.class).typed(Documented.class));

		assertThat(query)
			.contains(INSTANCE_DOCUMENTED, SubjectReflection.INSTANCE_SPECIAL, INSTANCE_DOCUMENTED)
			.doesNotContain(EXAMPLE_STRING, SubjectReflection.INSTANCE_OTHER, INSTANCE_RETENTION,
				SubjectReflection.INSTANCE_NULLABLE);
	}


}
