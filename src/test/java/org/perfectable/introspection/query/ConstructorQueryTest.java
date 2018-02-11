package org.perfectable.introspection.query;

import org.perfectable.introspection.Subject;
import org.perfectable.introspection.SubjectReflection;

import java.util.regex.Pattern;

import javassist.Modifier;
import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.AbstractQueryAssert.assertThat;

class ConstructorQueryTest {

	@Test
	void testUnrestricted() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class);

		assertThat(extracted)
			.containsExactly(SubjectReflection.CONSTRUCTOR_NO_ARGS,
				SubjectReflection.CONSTRUCTOR_STRING,
				SubjectReflection.CONSTRUCTOR_ANNOTATED,
				SubjectReflection.CONSTRUCTOR_PROTECTED);
	}

	@Test
	void testNamedPositive() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).named(Subject.class.getName());

		assertThat(extracted)
			.containsExactly(SubjectReflection.CONSTRUCTOR_NO_ARGS,
				SubjectReflection.CONSTRUCTOR_STRING,
				SubjectReflection.CONSTRUCTOR_ANNOTATED,
				SubjectReflection.CONSTRUCTOR_PROTECTED);
	}

	@Test
	void testNamedNegative() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).named("Something Other");

		assertThat(extracted)
			.isEmpty();
	}

	@Test
	void testNameMatchingPositive() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).nameMatching(Pattern.compile(".*Subj[ect]{3}$"));

		assertThat(extracted)
			.containsExactly(SubjectReflection.CONSTRUCTOR_NO_ARGS,
				SubjectReflection.CONSTRUCTOR_STRING,
				SubjectReflection.CONSTRUCTOR_ANNOTATED,
				SubjectReflection.CONSTRUCTOR_PROTECTED);
	}

	@Test
	void testFilterParameterCount() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).filter(method -> method.getParameterCount() == 0);

		assertThat(extracted)
			.isSingleton(SubjectReflection.CONSTRUCTOR_NO_ARGS);
	}

	@Test
	void testParametersByLength() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).parameters(ParametersFilter.count(1));

		assertThat(extracted)
			.containsExactly(SubjectReflection.CONSTRUCTOR_STRING,
				SubjectReflection.CONSTRUCTOR_ANNOTATED);
	}

	@Test
	void testParametersByType() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).parameters(String.class);

		assertThat(extracted)
			.isSingleton(SubjectReflection.CONSTRUCTOR_STRING);
	}

	@Test
	void testFilterDeclaringClassPositive() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class)
				.filter(constructor -> Subject.class.equals(constructor.getDeclaringClass()));

		assertThat(extracted)
			.containsExactly(SubjectReflection.CONSTRUCTOR_NO_ARGS,
				SubjectReflection.CONSTRUCTOR_STRING,
				SubjectReflection.CONSTRUCTOR_ANNOTATED,
				SubjectReflection.CONSTRUCTOR_PROTECTED);
	}

	@Test
	void testAnnotatedWithClass() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).annotatedWith(Subject.Special.class);

		assertThat(extracted)
			.isSingleton(SubjectReflection.CONSTRUCTOR_ANNOTATED);
	}

	@Test
	void testAnnotatedWith() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class).annotatedWith(AnnotationFilter.of(Subject.Special.class));

		assertThat(extracted)
			.isSingleton(SubjectReflection.CONSTRUCTOR_ANNOTATED);
	}

	@Test
	void testRequiringModifier() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class)
				.requiringModifier(Modifier.PROTECTED);

		assertThat(extracted)
			.isSingleton(SubjectReflection.CONSTRUCTOR_PROTECTED);
	}

	@Test
	void testExcludingModifier() {
		ConstructorQuery<Subject> extracted =
			ConstructorQuery.of(Subject.class)
				.excludingModifier(Modifier.PROTECTED);

		assertThat(extracted)
			.containsExactly(SubjectReflection.CONSTRUCTOR_NO_ARGS,
				SubjectReflection.CONSTRUCTOR_STRING,
				SubjectReflection.CONSTRUCTOR_ANNOTATED);
	}
}
