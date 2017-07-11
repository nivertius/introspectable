package org.perfectable.introspection;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassIntrospectionTest {

	@Test
	void testFieldQuery() {
		Iterable<Field> fields = ClassIntrospection.of(Subject.class).fields()
				.named(SubjectReflection.STATIC_FIELD.getName());

		assertThat(fields)
			.containsExactlyInAnyOrder(SubjectReflection.STATIC_FIELD);
	}

}
