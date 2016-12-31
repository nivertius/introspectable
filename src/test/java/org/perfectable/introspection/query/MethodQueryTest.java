package org.perfectable.introspection.query;

import org.perfectable.introspection.Methods;
import org.perfectable.introspection.Subject;

import javax.annotation.Nullable;

import javassist.Modifier;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MethodQueryTest {
	@Test
	public void testNamed() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).named("noResultNoArgument");

		assertThat(extracted).containsExactly(Subject.NO_RESULT_NO_ARGUMENT);
	}

	@Test
	public void testFilterParameterCount() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).filter(method -> method.getParameterCount() == 1);

		assertThat(extracted)
				.containsExactlyInAnyOrder(Subject.NO_RESULT_SINGLE_ARGUMENT, Subject.WITH_RESULT_SINGLE_ARGUMENT,
						Subject.NO_RESULT_VARARGS_ARGUMENT, Subject.WITH_RESULT_VARARGS_ARGUMENT,
						Methods.OBJECT_WAIT_TIMEOUT, Methods.OBJECT_EQUALS);
	}

	@Test
	public void testParametersByLength() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).parameters(parameters -> parameters.length == 1);

		assertThat(extracted)
				.containsExactlyInAnyOrder(Subject.NO_RESULT_SINGLE_ARGUMENT, Subject.WITH_RESULT_SINGLE_ARGUMENT,
						Subject.NO_RESULT_VARARGS_ARGUMENT, Subject.WITH_RESULT_VARARGS_ARGUMENT,
						Methods.OBJECT_WAIT_TIMEOUT, Methods.OBJECT_EQUALS);
	}

	@Test
	public void testParametersByType() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).parameters(long.class);

		assertThat(extracted)
				.containsExactlyInAnyOrder(Methods.OBJECT_WAIT_TIMEOUT);
	}

	@Test
	public void testFilterDeclaringClass() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).filter(method -> Object.class.equals(method.getDeclaringClass()));

		assertThat(extracted)
				.containsExactlyInAnyOrder(Methods.OBJECT_HASH_CODE, Methods.OBJECT_EQUALS,
						Methods.OBJECT_FINALIZE,
						Methods.OBJECT_NOTIFY, Methods.OBJECT_NOTIFY_ALL,
						Methods.OBJECT_WAIT, Methods.OBJECT_WAIT_TIMEOUT, Methods.OBJECT_WAIT_NANOSECONDS,
						Methods.OBJECT_GET_CLASS, Methods.OBJECT_CLONE, Methods.OBJECT_TO_STRING,
						Methods.OBJECT_REGISTER_NATIVES);
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testTyped() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).typed(Object.class);

		assertThat(extracted)
				.containsExactlyInAnyOrder(
						Subject.WITH_RESULT_NO_ARGUMENT, Subject.WITH_RESULT_SINGLE_ARGUMENT,
						Subject.WITH_RESULT_DOUBLE_ARGUMENT, Subject.WITH_RESULT_TRIPLE_ARGUMENT,
						Subject.WITH_RESULT_VARARGS_ARGUMENT, Subject.ANNOTATED_WITH_NULLABLE,
						Methods.OBJECT_CLONE
				);
	}

	@Test
	public void testReturning() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).returning(Object.class);

		assertThat(extracted)
				.containsExactlyInAnyOrder(
						Subject.WITH_RESULT_NO_ARGUMENT, Subject.WITH_RESULT_SINGLE_ARGUMENT,
						Subject.WITH_RESULT_DOUBLE_ARGUMENT, Subject.WITH_RESULT_TRIPLE_ARGUMENT,
						Subject.WITH_RESULT_VARARGS_ARGUMENT, Subject.ANNOTATED_WITH_NULLABLE,
						Methods.OBJECT_CLONE
				);
	}

	@Test
	public void testReturningBoolean() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).returning(boolean.class);

		assertThat(extracted)
				.containsExactlyInAnyOrder(Methods.OBJECT_EQUALS);
	}

	@Test
	public void testReturningVoid() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).returningVoid();

		assertThat(extracted)
				.containsExactlyInAnyOrder(Subject.NO_RESULT_NO_ARGUMENT, Subject.NO_RESULT_SINGLE_ARGUMENT,
						Subject.NO_RESULT_DOUBLE_ARGUMENT, Subject.NO_RESULT_TRIPLE_ARGUMENT,
						Subject.NO_RESULT_VARARGS_ARGUMENT, Subject.METHOD_PROTECTED,
						Methods.OBJECT_WAIT, Methods.OBJECT_WAIT_TIMEOUT, Methods.OBJECT_WAIT_NANOSECONDS,
						Methods.OBJECT_FINALIZE, Methods.OBJECT_REGISTER_NATIVES, Methods.OBJECT_NOTIFY,
						Methods.OBJECT_NOTIFY_ALL);
	}

	@Test
	public void testAnnotatedWithClass() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).annotatedWith(Nullable.class);

		assertThat(extracted)
				.containsExactlyInAnyOrder(Subject.ANNOTATED_WITH_NULLABLE);
	}

	@Test
	public void testAnnotatedWith() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).annotatedWith(AnnotationFilter.of(Nullable.class));

		assertThat(extracted)
				.containsExactlyInAnyOrder(Subject.ANNOTATED_WITH_NULLABLE);
	}

	@Test
	public void testExcludingModifier() {
		MethodQuery extracted =
				MethodQuery.of(Subject.class).excludingModifier(Modifier.PUBLIC);

		assertThat(extracted)
				.containsExactlyInAnyOrder(Methods.OBJECT_FINALIZE, Methods.OBJECT_CLONE,
						Methods.OBJECT_REGISTER_NATIVES, Subject.METHOD_PROTECTED);
	}
}
