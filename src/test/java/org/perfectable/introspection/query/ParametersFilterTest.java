package org.perfectable.introspection.query;

import org.perfectable.introspection.SubjectReflection;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;


class ParametersFilterTest {
	@Test
	void acceptingPrimitive() {
		ParametersFilter filter = ParametersFilter.typesAccepted(int.class);

		assertThat(filter.matches(SubjectReflection.NO_RESULT_NO_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT)).isFalse();
	}

	@Test
	void acceptingSingle() {
		ParametersFilter filter = ParametersFilter.typesAccepted(String.class);

		assertThat(filter.matches(SubjectReflection.NO_RESULT_NO_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT)).isFalse();
	}

	@Test
	void acceptingDouble() {
		ParametersFilter filter = ParametersFilter.typesAccepted(String.class, Number.class);

		assertThat(filter.matches(SubjectReflection.NO_RESULT_NO_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT)).isTrue();
	}

	@Test
	void exactPrimitive() {
		ParametersFilter filter = ParametersFilter.typesExact(int.class);

		assertThat(filter.matches(SubjectReflection.NO_RESULT_NO_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT)).isFalse();
	}

	@Test
	void exactSingle() {
		ParametersFilter filter = ParametersFilter.typesExact(String.class);

		assertThat(filter.matches(SubjectReflection.NO_RESULT_NO_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT)).isFalse();
	}

	@Test
	void exactDouble() {
		ParametersFilter filter = ParametersFilter.typesExact(String.class, Number.class);

		assertThat(filter.matches(SubjectReflection.NO_RESULT_NO_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT)).isFalse();
	}

	@Test
	void count1() {
		ParametersFilter filter = ParametersFilter.count(1);

		assertThat(filter.matches(SubjectReflection.NO_RESULT_NO_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT)).isFalse();
	}

	@Test
	void count2() {
		ParametersFilter filter = ParametersFilter.count(2);

		assertThat(filter.matches(SubjectReflection.NO_RESULT_NO_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT)).isTrue();
	}

	@Test
	void count3() {
		ParametersFilter filter = ParametersFilter.count(3); // SUPPRESS MagicNumber

		assertThat(filter.matches(SubjectReflection.NO_RESULT_NO_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)).isFalse();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)).isTrue();
		assertThat(filter.matches(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT)).isTrue();
	}

}
