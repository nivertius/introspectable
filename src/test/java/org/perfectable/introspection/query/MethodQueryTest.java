package org.perfectable.introspection.query;

import org.perfectable.introspection.SimpleReflections;
import org.perfectable.introspection.Subject;
import org.perfectable.introspection.SubjectReflection;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import javassist.Modifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodQueryTest {

	private static final Predicate<Method> JACOCO_EXCLUSION =
			method -> !method.getName().equals("$jacocoInit");

	@Test
	public void testNamed() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).named("noResultNoArgument");

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactly(SubjectReflection.NO_RESULT_NO_ARGUMENT);
	}

	@Test
	public void testFilterParameterCount() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).filter(method -> method.getParameterCount() == 1);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
						SubjectReflection.NO_RESULT_VARARGS_ARGUMENT, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
						SimpleReflections.OBJECT_WAIT_TIMEOUT, SimpleReflections.OBJECT_EQUALS);
	}

	@Test
	public void testParametersByLength() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).parameters(parameters -> parameters.length == 1);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
						SubjectReflection.NO_RESULT_VARARGS_ARGUMENT, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
						SimpleReflections.OBJECT_WAIT_TIMEOUT, SimpleReflections.OBJECT_EQUALS);
	}

	@Test
	public void testParametersByType() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).parameters(long.class);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SimpleReflections.OBJECT_WAIT_TIMEOUT);
	}

	@Test
	public void testFilterDeclaringClass() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).filter(method -> Object.class.equals(method.getDeclaringClass()));

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SimpleReflections.OBJECT_HASH_CODE, SimpleReflections.OBJECT_EQUALS,
						SimpleReflections.OBJECT_FINALIZE,
						SimpleReflections.OBJECT_NOTIFY, SimpleReflections.OBJECT_NOTIFY_ALL,
						SimpleReflections.OBJECT_WAIT, SimpleReflections.OBJECT_WAIT_TIMEOUT, SimpleReflections.OBJECT_WAIT_NANOSECONDS,
						SimpleReflections.OBJECT_GET_CLASS, SimpleReflections.OBJECT_CLONE, SimpleReflections.OBJECT_TO_STRING,
						SimpleReflections.OBJECT_REGISTER_NATIVES);
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testTyped() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).typed(Object.class);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(
						SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
						SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
						SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT, SubjectReflection.ANNOTATED_WITH_NULLABLE,
						SimpleReflections.OBJECT_CLONE
				);
	}

	@Test
	public void testReturning() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).returning(Object.class);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(
						SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
						SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
						SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT, SubjectReflection.ANNOTATED_WITH_NULLABLE,
						SimpleReflections.OBJECT_CLONE
				);
	}

	@Test
	public void testReturningBoolean() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).returning(boolean.class);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SimpleReflections.OBJECT_EQUALS);
	}

	@Test
	public void testReturningVoid() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).returningVoid();

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SubjectReflection.NO_RESULT_NO_ARGUMENT, SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
						SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
						SubjectReflection.NO_RESULT_VARARGS_ARGUMENT, SubjectReflection.METHOD_PROTECTED,
						SimpleReflections.OBJECT_WAIT, SimpleReflections.OBJECT_WAIT_TIMEOUT, SimpleReflections.OBJECT_WAIT_NANOSECONDS,
						SimpleReflections.OBJECT_FINALIZE, SimpleReflections.OBJECT_REGISTER_NATIVES, SimpleReflections.OBJECT_NOTIFY,
						SimpleReflections.OBJECT_NOTIFY_ALL);
	}

	@Test
	public void testAnnotatedWithClass() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).annotatedWith(Nullable.class);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SubjectReflection.ANNOTATED_WITH_NULLABLE);
	}

	@Test
	public void testAnnotatedWith() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).annotatedWith(AnnotationFilter.of(Nullable.class));

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SubjectReflection.ANNOTATED_WITH_NULLABLE);
	}

	@Test
	public void testExcludingModifier() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).excludingModifier(Modifier.PUBLIC);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SimpleReflections.OBJECT_FINALIZE, SimpleReflections.OBJECT_CLONE,
						SimpleReflections.OBJECT_REGISTER_NATIVES, SubjectReflection.METHOD_PROTECTED);
	}
}
