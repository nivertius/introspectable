package org.perfectable.introspection;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("static-method")
public class IntrospectionTest {

	@SuppressWarnings("unused")
	private static class TestBean {
		private String privateStringField;
	}

	private static final Field PRIVATE_STRING_FIELD = extractField(TestBean.class, "privateStringField");

	@Test
	public void testFieldQuery() {
		Iterable<Field> fields = Introspection.of(TestBean.class).fields().named("privateStringField");

		assertThat(fields).containsExactly(PRIVATE_STRING_FIELD);
	}

	private static Field extractField(Class<?> beanClass, String fieldName) {
		try {
			return beanClass.getDeclaredField(fieldName);
		}
		catch (NoSuchFieldException | SecurityException e) {
			throw new AssertionError(e);
		}
	}

}
