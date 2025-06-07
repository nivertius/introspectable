package org.perfectable.introspection.query; // SUPPRESS LENGTH

import org.perfectable.introspection.ObjectMethods;

import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javassist.Modifier;
import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.AbstractQueryAssert.assertThat;

class MethodQueryTest {

	@SuppressWarnings({"UnnecessaryLambda", "Indentation"})
	private static final Predicate<Method> JACOCO_EXCLUSION =
		method -> !method.getName().equals("$jacocoInit");
	@SuppressWarnings({"UnnecessaryLambda", "Indentation"})
	private static final Predicate<Method> IGNORED_OBJECT_METHODS =
		method -> !(method.getDeclaringClass().equals(Object.class) && (
				method.getName().equals("registerNatives") || method.getName().equals("wait0")));
	private static final String EXAMPLE_STRING = "testString";

	@Test
	void testUnrestricted() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.filteredOn(IGNORED_OBJECT_METHODS)
			.sortsCorrectlyWith(Comparator.comparing(Method::toString))
			.containsExactly(SubjectReflection.NO_RESULT_NO_ARGUMENT,
				SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
				SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT,
				SubjectReflection.METHOD_PROTECTED,
				SubjectReflection.METHOD_PACKAGE,
				SubjectReflection.METHOD_PRIVATE,
				SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.ANNOTATED_METHOD,
				SubjectReflection.TO_STRING,
				ObjectMethods.EQUALS, ObjectMethods.HASH_CODE,
				ObjectMethods.GET_CLASS,
				ObjectMethods.CLONE,
				ObjectMethods.WAIT, ObjectMethods.WAIT_TIMEOUT,
				ObjectMethods.WAIT_NANOSECONDS, ObjectMethods.FINALIZE,
				ObjectMethods.NOTIFY, ObjectMethods.NOTIFY_ALL,
				ObjectMethods.TO_STRING)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.STATIC_FIELD);
	}


	@Test
	void testInheritanceChain() {
		MethodQuery extracted =
			MethodQuery.of(InheritanceQuery.of(Subject.class).upToExcluding(Object.class));

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.filteredOn(IGNORED_OBJECT_METHODS)
			.containsExactly(SubjectReflection.NO_RESULT_NO_ARGUMENT,
				SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
				SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT,
				SubjectReflection.METHOD_PROTECTED,
				SubjectReflection.METHOD_PACKAGE,
				SubjectReflection.METHOD_PRIVATE,
				SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.ANNOTATED_METHOD,
				SubjectReflection.TO_STRING)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testNamed() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).named("noResultNoArgument");

		assertThat(extracted)
			.isSingleton(SubjectReflection.NO_RESULT_NO_ARGUMENT)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testNameMatching() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).nameMatching(Pattern.compile(".*ResultDouble.*"));

		assertThat(extracted)
			.sortsCorrectlyWith(Comparator.comparing(Method::toString))
			.containsExactly(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testFilterParameterCount() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).filter(method -> method.getParameterCount() == 1);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.filteredOn(IGNORED_OBJECT_METHODS)
			.sortsCorrectlyWith(Comparator.comparing(Method::toString))
			.containsExactly(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_ARGUMENT, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				ObjectMethods.WAIT_TIMEOUT, ObjectMethods.EQUALS)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testParametersByLength() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).parameters(ParametersFilter.count(1));

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.filteredOn(IGNORED_OBJECT_METHODS)
			.sortsCorrectlyWith(Comparator.comparing(Method::toString))
			.containsExactly(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_ARGUMENT, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				ObjectMethods.WAIT_TIMEOUT, ObjectMethods.EQUALS)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testParametersByType() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).parameters(long.class);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.filteredOn(IGNORED_OBJECT_METHODS)
			.containsExactly(ObjectMethods.WAIT_TIMEOUT)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testFilterDeclaringClass() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).filter(method -> Object.class.equals(method.getDeclaringClass()));

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.filteredOn(IGNORED_OBJECT_METHODS)
			.containsExactly(ObjectMethods.HASH_CODE, ObjectMethods.EQUALS,
				ObjectMethods.FINALIZE,
				ObjectMethods.NOTIFY, ObjectMethods.NOTIFY_ALL,
				ObjectMethods.WAIT, ObjectMethods.WAIT_TIMEOUT,
				ObjectMethods.WAIT_NANOSECONDS,
				ObjectMethods.GET_CLASS, ObjectMethods.CLONE,
				ObjectMethods.TO_STRING)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
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
				SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT, SubjectReflection.ANNOTATED_METHOD,
				SubjectReflection.TO_STRING,
				ObjectMethods.CLONE, ObjectMethods.TO_STRING,
				ObjectMethods.GET_CLASS)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testReturningExact() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).returning(TypeFilter.exact(Object.class));

		assertThat(extracted)
			.containsExactly(
				SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT, SubjectReflection.ANNOTATED_METHOD,
				ObjectMethods.CLONE)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testReturningBoolean() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).returning(boolean.class);

		assertThat(extracted)
			.isSingleton(ObjectMethods.EQUALS)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testReturningVoid() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).returningVoid();

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.filteredOn(IGNORED_OBJECT_METHODS)
			.containsExactly(SubjectReflection.NO_RESULT_NO_ARGUMENT,
				SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
				SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT,
				SubjectReflection.METHOD_PROTECTED,
				SubjectReflection.METHOD_PACKAGE,
				SubjectReflection.METHOD_PRIVATE,
				ObjectMethods.WAIT, ObjectMethods.WAIT_TIMEOUT,
				ObjectMethods.WAIT_NANOSECONDS,
				ObjectMethods.FINALIZE,
				ObjectMethods.NOTIFY,
				ObjectMethods.NOTIFY_ALL)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testNotOverriden() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class)
				.notOverridden();

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.filteredOn(IGNORED_OBJECT_METHODS)
			.sortsCorrectlyWith(Comparator.comparing(Method::toString))
			.containsExactly(SubjectReflection.NO_RESULT_NO_ARGUMENT,
				SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
				SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT,
				SubjectReflection.METHOD_PROTECTED,
				SubjectReflection.METHOD_PACKAGE,
				SubjectReflection.METHOD_PRIVATE,
				SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.ANNOTATED_METHOD,
				SubjectReflection.TO_STRING,
				ObjectMethods.EQUALS, ObjectMethods.HASH_CODE,
				ObjectMethods.GET_CLASS,
				ObjectMethods.CLONE,
				ObjectMethods.WAIT, ObjectMethods.WAIT_TIMEOUT,
				ObjectMethods.WAIT_NANOSECONDS, ObjectMethods.FINALIZE,
				ObjectMethods.NOTIFY, ObjectMethods.NOTIFY_ALL)
			.doesNotContain(EXAMPLE_STRING, null, ObjectMethods.TO_STRING,
				SubjectReflection.STATIC_FIELD);
	}


	@Test
	void testNotOverridenExtension() {
		MethodQuery extracted =
			MethodQuery.of(Subject.Extension.class)
				.notOverridden();

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.filteredOn(IGNORED_OBJECT_METHODS)
			.containsExactly(SubjectReflection.Extension.NO_RESULT_NO_ARGUMENT,
				SubjectReflection.NO_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT, SubjectReflection.NO_RESULT_STRING_ARGUMENT,
				SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT,
				SubjectReflection.Extension.METHOD_PROTECTED,
				SubjectReflection.Extension.METHOD_PACKAGE,
				SubjectReflection.Extension.METHOD_PRIVATE,
				SubjectReflection.METHOD_PRIVATE,
				SubjectReflection.WITH_RESULT_NO_ARGUMENT, SubjectReflection.WITH_RESULT_SINGLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_DOUBLE_ARGUMENT, SubjectReflection.WITH_RESULT_TRIPLE_ARGUMENT,
				SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.ANNOTATED_METHOD,
				SubjectReflection.TO_STRING,
				ObjectMethods.EQUALS, ObjectMethods.HASH_CODE,
				ObjectMethods.GET_CLASS,
				ObjectMethods.CLONE,
				ObjectMethods.WAIT, ObjectMethods.WAIT_TIMEOUT,
				ObjectMethods.WAIT_NANOSECONDS, ObjectMethods.FINALIZE,
				ObjectMethods.NOTIFY, ObjectMethods.NOTIFY_ALL)
			.doesNotContain(EXAMPLE_STRING, null, ObjectMethods.TO_STRING,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testAnnotatedWithClass() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).annotatedWith(Subject.OtherAnnotation.class);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.ANNOTATED_METHOD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testAnnotatedWith() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class).annotatedWith(AnnotationFilter.single(Subject.OtherAnnotation.class));

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.contains(SubjectReflection.ANNOTATED_METHOD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testRequiringModifier() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class)
				.requiringModifier(Modifier.PROTECTED);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.sortsCorrectlyWith(Comparator.comparing(Method::toString))
			.containsExactly(ObjectMethods.FINALIZE, ObjectMethods.CLONE,
				SubjectReflection.METHOD_PROTECTED)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}

	@Test
	void testExcludingModifier() {
		MethodQuery extracted =
			MethodQuery.of(Subject.class)
				.excludingModifier(Modifier.PUBLIC);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.filteredOn(IGNORED_OBJECT_METHODS)
			.containsExactly(ObjectMethods.FINALIZE, ObjectMethods.CLONE,
				SubjectReflection.METHOD_PROTECTED,
				SubjectReflection.METHOD_PACKAGE,
				SubjectReflection.METHOD_PRIVATE)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.WITH_RESULT_VARARGS_ARGUMENT,
				SubjectReflection.STATIC_FIELD);
	}
}
