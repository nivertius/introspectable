package org.perfectable.introspection.query;

import org.perfectable.introspection.Subject;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationQueryTest {

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
	public void testEmpty() {
		AnnotationQuery<?> chain = AnnotationQuery.empty();

		assertThat(chain)
				.containsExactly();
	}

	@Test
	public void testEmptyFilter() {
		AnnotationQuery<?> chain = AnnotationQuery.empty()
				.filter(annotation -> annotation.toString().equals("None"));

		assertThat(chain)
				.containsExactly();
	}

	@Test
	public void testString() {
		AnnotationQuery<?> chain = AnnotationQuery.of(String.class);

		assertThat(chain)
				.containsExactly();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSubjectUnrestricted() {
		AnnotationQuery<Annotation> chain = AnnotationQuery.of(Subject.class);

		assertThat(chain)
				.extracting(Annotation::annotationType)
				.containsExactlyInAnyOrder(Subject.Special.class, Subject.OtherAnnotation.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSubjectTyped() {
		AnnotationQuery<?> chain = AnnotationQuery.of(Subject.class)
				.typed(Subject.Special.class);

		assertThat(chain)
				.extracting(Annotation::annotationType)
				.containsExactlyInAnyOrder(Subject.Special.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testPredicated() {
		AnnotationQuery<?> chain = AnnotationQuery.of(Subject.class)
				.filter(a -> a.annotationType().getSimpleName().startsWith("O"));

		assertThat(chain)
				.extracting(Annotation::annotationType)
				.containsExactlyInAnyOrder(Subject.OtherAnnotation.class);
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testJoin() {
		AnnotationQuery<Subject.Special> first =
				AnnotationQuery.of(Subject.class).typed(Subject.Special.class);
		AnnotationQuery<Documented> second =
				AnnotationQuery.of(Retention.class).typed(Documented.class);
		AnnotationQuery<Annotation> chain = first.join(second);

		assertThat(chain)
				.extracting(Annotation::annotationType)
				.containsExactlyInAnyOrder(Subject.Special.class, Documented.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testJoinMultiple() {
		AnnotationQuery<Annotation> chain = AnnotationQuery.empty()
				.join(AnnotationQuery.of(Retention.class).typed(Documented.class))
				.join(AnnotationQuery.of(Subject.class).typed(Subject.Special.class))
				.join(AnnotationQuery.of(Documented.class).typed(Documented.class));

		assertThat(chain)
				.extracting(Annotation::annotationType)
				.containsExactlyInAnyOrder(Subject.Special.class, Documented.class, Documented.class);
	}

}
