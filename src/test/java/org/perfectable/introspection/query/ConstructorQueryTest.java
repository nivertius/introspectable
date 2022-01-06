package org.perfectable.introspection.query;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.regex.Pattern;

import javassist.Modifier;
import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.AbstractQueryAssert.assertThat;

class ConstructorQueryTest {

	private static final String EXAMPLE_STRING = "testString";

	@Test
	void testUnrestricted() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class);

		assertThat(extracted)
			.sortsCorrectlyWith(Comparator.comparing(Constructor::toString))
			.containsExactly(SubjectReflection.CONSTRUCTOR_NO_ARGS,
				SubjectReflection.CONSTRUCTOR_STRING,
				SubjectReflection.CONSTRUCTOR_ANNOTATED,
				SubjectReflection.CONSTRUCTOR_PROTECTED)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testNamedPositive() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).named(Subject.class.getName());

		assertThat(extracted)
			.sortsCorrectlyWith(Comparator.comparing(Constructor::toString))
			.containsExactly(SubjectReflection.CONSTRUCTOR_NO_ARGS,
				SubjectReflection.CONSTRUCTOR_STRING,
				SubjectReflection.CONSTRUCTOR_ANNOTATED,
				SubjectReflection.CONSTRUCTOR_PROTECTED)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testNamedNegative() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).named("Something Other");

		assertThat(extracted)
			.isEmpty()
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testNameMatchingPositive() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).nameMatching(Pattern.compile(".*Subj[ect]{3}$"));

		assertThat(extracted)
			.sortsCorrectlyWith(Comparator.comparing(Constructor::toString))
			.containsExactly(SubjectReflection.CONSTRUCTOR_NO_ARGS,
				SubjectReflection.CONSTRUCTOR_STRING,
				SubjectReflection.CONSTRUCTOR_ANNOTATED,
				SubjectReflection.CONSTRUCTOR_PROTECTED)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testFilterParameterCount() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).filter(method -> method.getParameterCount() == 0);

		assertThat(extracted)
			.isSingleton(SubjectReflection.CONSTRUCTOR_NO_ARGS)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testParametersByLength() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).parameters(ParametersFilter.count(1));

		assertThat(extracted)
			.sortsCorrectlyWith(Comparator.comparing(Constructor::toString))
			.containsExactly(SubjectReflection.CONSTRUCTOR_STRING,
				SubjectReflection.CONSTRUCTOR_ANNOTATED)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testParametersByType() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).parameters(String.class);

		assertThat(extracted)
			.sortsCorrectlyWith(Comparator.comparing(Constructor::toString))
			.isSingleton(SubjectReflection.CONSTRUCTOR_STRING)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testFilterDeclaringClassPositive() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class)
				.filter(constructor -> Subject.class.equals(constructor.getDeclaringClass()));

		assertThat(extracted)
			.sortsCorrectlyWith(Comparator.comparing(Constructor::toString))
			.containsExactly(SubjectReflection.CONSTRUCTOR_NO_ARGS,
				SubjectReflection.CONSTRUCTOR_STRING,
				SubjectReflection.CONSTRUCTOR_ANNOTATED,
				SubjectReflection.CONSTRUCTOR_PROTECTED)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testAnnotatedWithClass() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).annotatedWith(Subject.Special.class);

		assertThat(extracted)
			.isSingleton(SubjectReflection.CONSTRUCTOR_ANNOTATED)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testAnnotatedWith() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).annotatedWith(AnnotationFilter.single(Subject.Special.class));

		assertThat(extracted)
			.isSingleton(SubjectReflection.CONSTRUCTOR_ANNOTATED)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testRequiringModifier() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class)
				.requiringModifier(Modifier.PROTECTED);

		assertThat(extracted)
			.isSingleton(SubjectReflection.CONSTRUCTOR_PROTECTED)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testExcludingModifier() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class)
				.excludingModifier(Modifier.PROTECTED);

		assertThat(extracted)
			.sortsCorrectlyWith(Comparator.comparing(Constructor::toString))
			.containsExactly(SubjectReflection.CONSTRUCTOR_NO_ARGS,
				SubjectReflection.CONSTRUCTOR_STRING,
				SubjectReflection.CONSTRUCTOR_ANNOTATED)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}
}
