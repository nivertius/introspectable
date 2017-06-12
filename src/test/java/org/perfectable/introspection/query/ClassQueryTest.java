package org.perfectable.introspection.query;

import org.perfectable.introspection.Subject;
import org.perfectable.introspection.SubjectReflection;

import javassist.CtClass;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// SUPPRESS NEXT 1 SuppressWarnings
@SuppressWarnings("unchecked")
class ClassQueryTest {
	private static final String PACKAGE_NAME = "org.perfectable";

	@Test
	void testClassLoaderPackage() {
		ClassQuery<Object> query = ClassQuery.of(ClassQueryTest.class.getClassLoader())
			.inPackage(PACKAGE_NAME);

		assertThat(query)
			.contains(ClassQueryTest.class, ClassQuery.class, Subject.class, SubjectReflection.class)
			.doesNotContain(String.class, CtClass.class);
	}

	@Test
	void testClassLoaderAnnotation() {
		ClassQuery<Object> query = ClassQuery.of(ClassQueryTest.class.getClassLoader())
			.annotatedWith(Subject.Special.class);

		assertThat(query)
			.contains(Subject.class)
			.doesNotContain(String.class, CtClass.class, ClassQuery.class);
	}

	@Test
	void testClassLoaderSubtype() {
		ClassQuery<Object> query = ClassQuery.of(ClassQueryTest.class.getClassLoader())
			.subtypeOf(AbstractQuery.class);

		assertThat(query)
			.contains(ClassQuery.class, MethodQuery.class)
			.doesNotContain(String.class, Subject.class, CtClass.class);
	}
}
