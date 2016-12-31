package org.perfectable.introspection.query;

import org.perfectable.introspection.Subject;
import org.perfectable.introspection.SubjectReflection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Predicate;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FieldQueryTest {
	private static final Predicate<Field> JACOCO_EXCLUSION =
			method -> !method.getName().equals("$jacocoData");

	@Test
	public void testUnrestricted() {
		FieldQuery extracted =
				FieldQuery.of(Subject.class);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactly(SubjectReflection.STRING_FIELD, SubjectReflection.OBJECT_FIELD,
						SubjectReflection.PROTECTED_NUMBER_FIELD, SubjectReflection.STATIC_FIELD);
	}

	@Test
	public void testNamed() {
		FieldQuery extracted =
				FieldQuery.of(Subject.class).named("stringField");

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactly(SubjectReflection.STRING_FIELD);
	}

	@Test
	public void testFilter() {
		FieldQuery extracted =
				FieldQuery.of(Subject.class)
						.filter(field -> (field.getModifiers() & Modifier.PUBLIC) > 0);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactly(SubjectReflection.STATIC_FIELD);
	}

	@Test
	public void testTyped() {
		FieldQuery extracted =
				FieldQuery.of(Subject.class)
						.typed(Number.class);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactly(SubjectReflection.PROTECTED_NUMBER_FIELD);
	}

	@Test
	public void testAnnotatedWith() {
		FieldQuery extracted =
				FieldQuery.of(Subject.class)
						.annotatedWith(Nullable.class);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactly(SubjectReflection.OBJECT_FIELD, SubjectReflection.STATIC_FIELD);
	}

	@Test
	public void testExcludingModifier() {
		FieldQuery extracted =
				FieldQuery.of(Subject.class)
						.excludingModifier(Modifier.FINAL)
						.excludingModifier(Modifier.PRIVATE);

		assertThat(extracted)
				.filteredOn(JACOCO_EXCLUSION)
				.containsExactly(SubjectReflection.STATIC_FIELD);
	}


}
