package org.perfectable.introspection.query;

import java.io.Serializable;
import java.net.URLClassLoader;
import java.util.Comparator;

import javassist.CtClass;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.AbstractQueryAssert.assertThat;

class ClassQueryTest {
	private static final String PACKAGE_NAME = "org.perfectable";

	abstract static class Methods {
		private static final String EXAMPLE_STRING = "testString";

		protected abstract ClassQuery<Object> createRawQuery();

		final ClassQuery<Object> createQuery() {
			return createRawQuery()
				.notInPackage("groovyjarjarantlr4"); // problematic package with big classes
		}

		@Test
		void inPackage() {
			ClassQuery<Object> query = createQuery()
				.inPackage(PACKAGE_NAME);

			assertThat(query)
				.sortsCorrectlyWith(Comparator.comparing(Class::toString))
				.contains(Subject.OtherAnnotation.class, AnnotationFilter.class,
					ClassQueryTest.class, ClassQuery.class, Subject.class, SubjectReflection.class)
				.doesNotContain(Serializable.class, String.class, CtClass.class, EXAMPLE_STRING);
		}

		@Test
		void annotatedWith() {
			ClassQuery<Object> query = createQuery()
				.annotatedWith(Subject.Special.class);

			assertThat(query)
				.isSingleton(Subject.class)
				.doesNotContain(Serializable.class, AnnotationFilter.class, String.class, CtClass.class,
					ClassQueryTest.class, ClassQuery.class, SubjectReflection.class, EXAMPLE_STRING);
		}

		@Test
		void subtypeOf() {
			ClassQuery<Object> query = createQuery()
				.subtypeOf(AbstractQuery.class);

			assertThat(query)
				.sortsCorrectlyWith(Comparator.comparing(Class::toString))
				.contains(ClassQuery.class, MethodQuery.class)
				.doesNotContain(Serializable.class, AnnotationFilter.class, String.class, CtClass.class,
					ClassQueryTest.class, Subject.class, SubjectReflection.class, EXAMPLE_STRING);
		}

		@Test
		void filter() {
			ClassQuery<Object> query = createQuery()
				.filter(Class::isInterface);

			assertThat(query)
				.sortsCorrectlyWith(Comparator.comparing(Class::toString))
				.contains(AnnotationFilter.class)
				.doesNotContain(String.class, CtClass.class, ClassQueryTest.class,
					ClassQuery.class, Subject.class, SubjectReflection.class, EXAMPLE_STRING);
		}

	}

	@SuppressWarnings("ClassCanBeStatic")
	@Nested
	class SystemClassLoader extends Methods {
		@Override
		protected ClassQuery<Object> createRawQuery() {
			return ClassQuery.system();
		}
	}

	@SuppressWarnings("ClassCanBeStatic")
	@Nested
	class OfClassLoader extends Methods {
		@Override
		protected ClassQuery<Object> createRawQuery() {
			@Nullable ClassLoader classLoader = ClassQueryTest.class.getClassLoader();
			Assumptions.assumeTrue(classLoader instanceof URLClassLoader);
			@SuppressWarnings("cast.unsafe")
			ClassLoader castedClassLoader = (@NonNull ClassLoader) classLoader;
			return ClassQuery.of(castedClassLoader);
		}
	}
}
