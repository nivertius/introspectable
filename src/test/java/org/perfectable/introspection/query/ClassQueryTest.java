package org.perfectable.introspection.query;

import org.perfectable.introspection.Subject;
import org.perfectable.introspection.SubjectReflection;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// SUPPRESS NEXT 1 SuppressWarnings
@SuppressWarnings("unchecked")
public class ClassQueryTest {
	private static final String PACKAGE_NAME = "org.perfectable";

	@Test
	public void testPackage() {
		ClassQuery<Object> query = ClassQuery.of(ClassQueryTest.class.getClassLoader())
			.inPackage(PACKAGE_NAME);

		assertThat(query)
			.contains(ClassQueryTest.class, ClassQuery.class, Subject.class, SubjectReflection.class);
	}

	@Test
	public void testSubtype() {
		ClassQuery<AbstractQuery<?, ?>> query = ClassQuery.of(ClassQueryTest.class.getClassLoader())
			.inPackage(PACKAGE_NAME)
			.subtypeOf(ClassQueryTest.<AbstractQuery<?, ?>>genericsCast(AbstractQuery.class));

		assertThat(query)
			.contains(ClassQueryTest.<ClassQuery<?>>genericsCast(ClassQuery.class));
	}

	private static <C> Class<C> genericsCast(Class<? super C> source) {
		return (Class<C>) source;
	}
}
