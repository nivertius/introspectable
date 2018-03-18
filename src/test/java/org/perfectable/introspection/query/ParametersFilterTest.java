package org.perfectable.introspection.query;

import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.ParametersFilterAssert.assertThat;

class ParametersFilterTest {
	@Test
	void acceptingPrimitive() {
		ParametersFilter filter = ParametersFilter.typesAccepted(int.class);

		assertThat(filter)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_NO_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT);
	}

	@Test
	void acceptingSingle() {
		ParametersFilter filter = ParametersFilter.typesAccepted(String.class);

		assertThat(filter)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_NO_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_STRING_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT);
	}

	@Test
	void acceptingDouble() {
		ParametersFilter filter = ParametersFilter.typesAccepted(String.class, Number.class);

		assertThat(filter)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_NO_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT);
	}

	@Test
	void exactPrimitive() {
		ParametersFilter filter = ParametersFilter.typesExact(int.class);

		assertThat(filter)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_NO_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT);
	}

	@Test
	void exactSingle() {
		ParametersFilter filter = ParametersFilter.typesExact(String.class);

		assertThat(filter)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_NO_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_STRING_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT);
	}

	@Test
	void exactDouble() {
		ParametersFilter filter = ParametersFilter.typesExact(String.class, Number.class);

		assertThat(filter)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_NO_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT);
	}

	@Test
	void count1() {
		ParametersFilter filter = ParametersFilter.count(1);

		assertThat(filter)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_NO_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_STRING_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT);
	}

	@Test
	void count2() {
		ParametersFilter filter = ParametersFilter.count(2);

		assertThat(filter)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_NO_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT);
	}

	@Test
	void count3() {
		ParametersFilter filter = ParametersFilter.count(3); // SUPPRESS MagicNumber

		assertThat(filter)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_NO_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT);
	}

	@Test
	void matchingArguments() {
		ParametersFilter filter = ParametersFilter.matchingArguments(null, 0); // SUPPRESS MagicNumber

		assertThat(filter)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_NO_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_PRIMITIVE_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_STRING_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_SINGLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_DOUBLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_STRING_NUMBER_ARGUMENT)
			.doesntMatchMethod(SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_VARARGS_ARGUMENT)
			.matchesMethod(SubjectReflection.NO_RESULT_VARARGS_DOUBLE_ARGUMENT);
	}

}
