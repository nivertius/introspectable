package org.perfectable.introspection;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ClassIntrospectionTest {

	@Test
	public void testFieldQuery() {
		Iterable<Field> fields = ClassIntrospection.of(Subject.class).fields()
				.named(SubjectReflection.STATIC_FIELD.getName());

		assertThat(fields).containsExactly(SubjectReflection.STATIC_FIELD);
	}

}
