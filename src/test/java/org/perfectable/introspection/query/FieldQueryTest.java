package org.perfectable.introspection.query;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.AbstractQueryAssert.assertThat;

class FieldQueryTest {
	private static final Predicate<Field> JACOCO_EXCLUSION =
		method -> !method.getName().equals("$jacocoData");
	private static final String EXAMPLE_STRING = "testString";

	@Test
	void testEmpty() {
		FieldQuery extracted =
			FieldQuery.empty();

		assertThat(extracted)
			.isEmpty();
	}

	@Test
	void testUnrestricted() {
		FieldQuery extracted =
			FieldQuery.of(Subject.class);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.STRING_FIELD, SubjectReflection.OBJECT_FIELD,
				SubjectReflection.PROTECTED_NUMBER_FIELD, SubjectReflection.STATIC_FIELD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testEmptyInterface() {
		FieldQuery extracted =
			FieldQuery.of(Serializable.class);

		assertThat(extracted)
			.isEmpty()
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testInterfaceWithStaticField() {
		FieldQuery extracted =
			FieldQuery.of(Subject.NestedInterface.class);

		assertThat(extracted)
			.isSingleton(SubjectReflection.NESTED_INTERFACE_FIELD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testJoined() {
		FieldQuery extracted =
			FieldQuery.of(Subject.class).join(FieldQuery.of(Subject.NestedInterface.class));

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.STRING_FIELD, SubjectReflection.OBJECT_FIELD,
				SubjectReflection.PROTECTED_NUMBER_FIELD, SubjectReflection.STATIC_FIELD,
				SubjectReflection.NESTED_INTERFACE_FIELD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testNamed() {
		FieldQuery extracted =
			FieldQuery.of(Subject.class).named("stringField");

		assertThat(extracted)
			.isSingleton(SubjectReflection.STRING_FIELD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testNameMatching() {
		FieldQuery extracted =
			FieldQuery.of(Subject.class).nameMatching(Pattern.compile(".*Field"));

		assertThat(extracted)
			.containsExactly(SubjectReflection.STRING_FIELD, SubjectReflection.STATIC_FIELD,
				SubjectReflection.OBJECT_FIELD, SubjectReflection.PROTECTED_NUMBER_FIELD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testFilter() {
		FieldQuery extracted =
			FieldQuery.of(Subject.class)
				.filter(field -> (field.getModifiers() & Modifier.PUBLIC) > 0);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.STATIC_FIELD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testTypedSimple() {
		FieldQuery extracted =
			FieldQuery.of(Subject.class)
				.typed(Number.class);

		assertThat(extracted)
			.isSingleton(SubjectReflection.PROTECTED_NUMBER_FIELD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testTypedExact() {
		FieldQuery extracted =
			FieldQuery.of(Subject.class)
				.typed(TypeFilter.exact(Object.class));

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.OBJECT_FIELD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testAnnotatedWith() {
		FieldQuery extracted =
			FieldQuery.of(Subject.class)
				.annotatedWith(Nullable.class);

		assertThat(extracted)
			.containsExactly(SubjectReflection.OBJECT_FIELD, SubjectReflection.STATIC_FIELD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testRequiringModifier() {
		FieldQuery extracted =
			FieldQuery.of(Subject.class)
				.requiringModifier(Modifier.FINAL)
				.requiringModifier(Modifier.PROTECTED);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.PROTECTED_NUMBER_FIELD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}

	@Test
	void testExcludingModifier() {
		FieldQuery extracted =
			FieldQuery.of(Subject.class)
				.excludingModifier(Modifier.FINAL)
				.excludingModifier(Modifier.PRIVATE);

		assertThat(extracted)
			.filteredOn(JACOCO_EXCLUSION)
			.containsExactly(SubjectReflection.STATIC_FIELD)
			.doesNotContain(EXAMPLE_STRING, null, SubjectReflection.NO_RESULT_TRIPLE_ARGUMENT);
	}


}
