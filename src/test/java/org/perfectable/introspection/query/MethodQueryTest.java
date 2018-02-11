package org.perfectable.introspection.query;

import org.perfectable.introspection.ObjectMethods;
import org.perfectable.introspection.Subject;
import org.perfectable.introspection.SubjectReflection;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import javassist.Modifier;
import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.AbstractQueryAssert.assertThat;

class MethodQueryTest {

	private static final Predicate<Method> JACOCO_EXCLUSION =
		method -> !method.getName().equals("$jacocoInit");

	@Test
	void testUnrestricted() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.NO_RESULT_NO_ARGUMENT,
				SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
				SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT,
				SubjectReflection.METHOD_PROTECTED,
				SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.ANNOTATED_WITH_NULLABLE,
				SubjectReflection.TO_STRING,
				ObjectMethods.EQUALS, ObjectMethods.HASH_CODE,
				ObjectMethods.GET_CLASS,
				ObjectMethods.CLONE,
				ObjectMethods.WAIT, ObjectMethods.WAIT_TIMEOUT,
				ObjectMethods.WAIT_NANOSECONDS, ObjectMethods.FINALIZE,
				ObjectMethods.REGISTER_NATIVES,
				ObjectMethods.NOTIFY, ObjectMethods.NOTIFY_ALL,
				ObjectMethods.TO_STRING);
	}

	@Test
	void testNamed() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).named("noResultNoArgument");

		assertThat(extracted)
			.isSingleton(SubjectReflection.NO_RESULT_NO_ARGUMENT);
	}

	@Test
	void testNameMatching() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).nameMatching(Pattern.compile(".*ResultDouble.*"));

		assertThat(extracted)
			.containsExactly(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT);
	}

	@Test
	void testFilterParameterCount() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).filter(method -> method.getParameterCount() == 1);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_ARGUMENT, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				ObjectMethods.WAIT_TIMEOUT, ObjectMethods.EQUALS);
	}

	@Test
	void testParametersByLength() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).parameters(ParametersFilter.count(1));

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_ARGUMENT, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				ObjectMethods.WAIT_TIMEOUT, ObjectMethods.EQUALS);
	}

	@Test
	void testParametersByType() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).parameters(long.class);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(ObjectMethods.WAIT_TIMEOUT);
	}

	@Test
	void testFilterDeclaringClass() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).filter(method -> Object.class.equals(method.getDeclaringClass()));

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(ObjectMethods.HASH_CODE, ObjectMethods.EQUALS,
				ObjectMethods.FINALIZE,
				ObjectMethods.NOTIFY, ObjectMethods.NOTIFY_ALL,
				ObjectMethods.WAIT, ObjectMethods.WAIT_TIMEOUT,
				ObjectMethods.WAIT_NANOSECONDS,
				ObjectMethods.GET_CLASS, ObjectMethods.CLONE,
				ObjectMethods.TO_STRING,
				ObjectMethods.REGISTER_NATIVES);
	}

	@Test
	void testReturningSimple() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).returning(Object.class);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(
				SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT, SubjectReflection.ANNOTATED_WITH_NULLABLE,
				SubjectReflection.TO_STRING,
				ObjectMethods.CLONE, ObjectMethods.TO_STRING,
				ObjectMethods.GET_CLASS);
	}

	@Test
	void testReturningExact() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).returning(TypeFilter.exact(Object.class));

		assertThat(extracted)
			.containsExactly(
				SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT, SubjectReflection.ANNOTATED_WITH_NULLABLE,
				ObjectMethods.CLONE);
	}

	@Test
	void testReturningBoolean() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).returning(boolean.class);

		assertThat(extracted)
			.isSingleton(ObjectMethods.EQUALS);
	}

	@Test
	void testReturningVoid() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).returningVoid();

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.NO_RESULT_NO_ARGUMENT,
				SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
				SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT,
				SubjectReflection.METHOD_PROTECTED,
				ObjectMethods.WAIT, ObjectMethods.WAIT_TIMEOUT,
				ObjectMethods.WAIT_NANOSECONDS,
				ObjectMethods.FINALIZE, ObjectMethods.REGISTER_NATIVES,
				ObjectMethods.NOTIFY,
				ObjectMethods.NOTIFY_ALL);
	}

	@Test
	void testNotOverriden() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class)
				.notOverridden();

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.NO_RESULT_NO_ARGUMENT,
				SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
				SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT,
				SubjectReflection.METHOD_PROTECTED,
				SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.ANNOTATED_WITH_NULLABLE,
				SubjectReflection.TO_STRING,
				ObjectMethods.EQUALS, ObjectMethods.HASH_CODE,
				ObjectMethods.GET_CLASS,
				ObjectMethods.CLONE,
				ObjectMethods.WAIT, ObjectMethods.WAIT_TIMEOUT,
				ObjectMethods.WAIT_NANOSECONDS, ObjectMethods.FINALIZE,
				ObjectMethods.REGISTER_NATIVES,
				ObjectMethods.NOTIFY, ObjectMethods.NOTIFY_ALL)
			.doesNotContain(ObjectMethods.TO_STRING);
	}

	@Test
	void testAnnotatedWithClass() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).annotatedWith(Nullable.class);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.ANNOTATED_WITH_NULLABLE);
	}

	@Test
	void testAnnotatedWith() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).annotatedWith(AnnotationFilter.of(Nullable.class));

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.ANNOTATED_WITH_NULLABLE);
	}

	@Test
	void testRequiringModifier() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class)
				.requiringModifier(Modifier.PROTECTED);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(ObjectMethods.FINALIZE, ObjectMethods.CLONE,
				SubjectReflection.METHOD_PROTECTED);
	}

	@Test
	void testExcludingModifier() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class)
				.excludingModifier(Modifier.PUBLIC);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(ObjectMethods.FINALIZE, ObjectMethods.CLONE,
				ObjectMethods.REGISTER_NATIVES, SubjectReflection.METHOD_PROTECTED);
	}
}
