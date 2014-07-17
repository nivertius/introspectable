package com.googlecode.perfectable.introspection;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;

import org.junit.Test;

import com.google.common.base.Throwables;

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
		catch(NoSuchFieldException | SecurityException e) {
			throw Throwables.propagate(e);
		}
	}
	
}
