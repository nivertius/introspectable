package org.perfectable.introspection.query;

import org.perfectable.introspection.Subject;
import org.perfectable.introspection.SubjectReflection;

import javassist.CtClass;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// SUPPRESS NEXT 1 SuppressWarnings
@SuppressWarnings("unchecked")
public class ClassQueryTest {
	private static final String PACKAGE_NAME = "org.perfectable";

	@Test
	public void testClassLoaderPackage() {
		ClassQuery<Object> query = ClassQuery.of(ClassQueryTest.class.getClassLoader())
			.inPackage(PACKAGE_NAME);

		assertThat(query)
			.contains(ClassQueryTest.class, ClassQuery.class, Subject.class, SubjectReflection.class)
			.doesNotContain(String.class, CtClass.class);
	}

	@Test
	public void testClassLoaderAnnotation() {
		ClassQuery<Object> query = ClassQuery.of(ClassQueryTest.class.getClassLoader())
			.annotatedWith(Subject.Special.class);

		assertThat(query)
			.contains(Subject.class)
			.doesNotContain(String.class, CtClass.class, ClassQuery.class);
	}

	@Test
	public void testClassLoaderSubtype() {
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
