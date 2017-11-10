package org.perfectable.introspection.query;

import org.perfectable.introspection.Subject;
import org.perfectable.introspection.SubjectReflection;

import javassist.CtClass;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ClassQueryTest {
	private static final String PACKAGE_NAME = "org.perfectable";

	abstract static class Methods {
		protected abstract ClassQuery<Object> createQuery();

		@Test
		void inPackage() {
			ClassQuery<Object> query = createQuery()
				.inPackage(PACKAGE_NAME);

			assertThat(query)
				.contains(ClassQueryTest.class, ClassQuery.class, Subject.class, SubjectReflection.class)
				.doesNotContain(String.class, CtClass.class);
		}

		@Test
		void annotatedWith() {
			ClassQuery<Object> query = createQuery()
				.annotatedWith(Subject.Special.class);

			assertThat(query)
				.contains(Subject.class)
				.doesNotContain(String.class, CtClass.class, ClassQuery.class);
		}


		@Test
		void subtypeOf() {
			ClassQuery<Object> query = createQuery()
				.subtypeOf(AbstractQuery.class);

			assertThat(query)
				.contains(ClassQuery.class, MethodQuery.class)
				.doesNotContain(String.class, Subject.class, CtClass.class);
		}

		@Test
		void filter() {
			ClassQuery<Object> query = createQuery()
				.filter(Class::isInterface);

			assertThat(query)
				.contains(AnnotationFilter.class)
				.doesNotContain(String.class, Subject.class, CtClass.class);
		}

	}

	@SuppressWarnings("ClassCanBeStatic")
	@Nested
	class All extends Methods {
		@Override
		protected ClassQuery<Object> createQuery() {
			return ClassQuery.all();
		}
	}

	@SuppressWarnings("ClassCanBeStatic")
	@Nested
	class OfClassLoader extends Methods {
		@Override
		protected ClassQuery<Object> createQuery() {
			return ClassQuery.of(ClassQueryTest.class.getClassLoader());
		}
	}
}
