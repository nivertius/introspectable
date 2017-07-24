package org.perfectable.introspection.query;

import org.perfectable.introspection.SimpleReflections;
import org.perfectable.introspection.Subject;
import org.perfectable.introspection.SubjectReflection;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import javassist.Modifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodQueryTest {

	private static final Predicate<Method> JACOCO_EXCLUSION =
			method -> !method.getName().equals("$jacocoInit");

	@Test
	void testNamed() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).named("noResultNoArgument");

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SubjectReflection.NO_RESULT_NO_ARGUMENT);
	}

	@Test
	void testNameMatching() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).nameMatching(Pattern.compile(".*ResultDouble.*"));

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactlyInAnyOrder(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT);
	}

	@Test
	void testFilterParameterCount() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).filter(method -> method.getParameterCount() == 1);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
						SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
						SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
						SubjectReflection.NO_RESULT_VARARGS_ARGUMENT, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
						SimpleReflections.OBJECT_WAIT_TIMEOUT, SimpleReflections.OBJECT_EQUALS);
	}

	@Test
	void testParametersByLength() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).parameters(ParametersFilter.count(1));

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
						SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
					SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
						SubjectReflection.NO_RESULT_VARARGS_ARGUMENT, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
						SimpleReflections.OBJECT_WAIT_TIMEOUT, SimpleReflections.OBJECT_EQUALS);
	}

	@Test
	void testParametersByType() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).parameters(long.class);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SimpleReflections.OBJECT_WAIT_TIMEOUT);
	}

	@Test
	void testFilterDeclaringClass() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).filter(method -> Object.class.equals(method.getDeclaringClass()));

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SimpleReflections.OBJECT_HASH_CODE, SimpleReflections.OBJECT_EQUALS,
						SimpleReflections.OBJECT_FINALIZE,
						SimpleReflections.OBJECT_NOTIFY, SimpleReflections.OBJECT_NOTIFY_ALL,
						SimpleReflections.OBJECT_WAIT, SimpleReflections.OBJECT_WAIT_TIMEOUT,
						SimpleReflections.OBJECT_WAIT_NANOSECONDS,
						SimpleReflections.OBJECT_GET_CLASS, SimpleReflections.OBJECT_CLONE,
						SimpleReflections.OBJECT_TO_STRING,
						SimpleReflections.OBJECT_REGISTER_NATIVES);
	}

	@Test
	void testReturningSimple() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).returning(Object.class);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(
						SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
						SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
						SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT, SubjectReflection.ANNOTATED_WITH_NULLABLE,
						SimpleReflections.OBJECT_CLONE, SimpleReflections.OBJECT_TO_STRING,
					    SimpleReflections.OBJECT_GET_CLASS);
	}

	@Test
	void testReturningExact() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).returning(TypeFilter.exact(Object.class));

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactlyInAnyOrder(
				SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT, SubjectReflection.ANNOTATED_WITH_NULLABLE,
				SimpleReflections.OBJECT_CLONE);
	}

	@Test
	void testReturningBoolean() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).returning(boolean.class);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SimpleReflections.OBJECT_EQUALS);
	}

	@Test
	void testReturningVoid() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).returningVoid();

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SubjectReflection.NO_RESULT_NO_ARGUMENT,
						SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
						SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
						SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
						SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT,
						SubjectReflection.NO_RESULT_VARARGS_ARGUMENT,
						SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT,
						SubjectReflection.METHOD_PROTECTED,
						SimpleReflections.OBJECT_WAIT, SimpleReflections.OBJECT_WAIT_TIMEOUT,
						SimpleReflections.OBJECT_WAIT_NANOSECONDS,
						SimpleReflections.OBJECT_FINALIZE, SimpleReflections.OBJECT_REGISTER_NATIVES,
						SimpleReflections.OBJECT_NOTIFY,
						SimpleReflections.OBJECT_NOTIFY_ALL);
	}

	@Test
	void testAnnotatedWithClass() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).annotatedWith(Nullable.class);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SubjectReflection.ANNOTATED_WITH_NULLABLE);
	}

	@Test
	void testAnnotatedWith() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).annotatedWith(AnnotationFilter.of(Nullable.class));

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SubjectReflection.ANNOTATED_WITH_NULLABLE);
	}

	@Test
	void testRequiringModifier() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class)
				.requiringModifier(Modifier.PROTECTED);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactlyInAnyOrder(SimpleReflections.OBJECT_FINALIZE, SimpleReflections.OBJECT_CLONE,
				SubjectReflection.METHOD_PROTECTED);
	}

	@Test
	void testExcludingModifier() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class)
					.excludingModifier(Modifier.PUBLIC);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactlyInAnyOrder(SimpleReflections.OBJECT_FINALIZE, SimpleReflections.OBJECT_CLONE,
						SimpleReflections.OBJECT_REGISTER_NATIVES, SubjectReflection.METHOD_PROTECTED);
	}
}
