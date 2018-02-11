package org.perfectable.introspection.query;

import org.perfectable.introspection.Subject;
import org.perfectable.introspection.SubjectReflection;

import java.io.Serializable;

import javassist.CtClass;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.AbstractQueryAssert.assertThat;

class ClassQueryTest {
	private static final String PACKAGE_NAME = "org.perfectable";

	abstract static class Methods {
		protected abstract ClassQuery<Object> createQuery();

		@Test
		void inPackage() {
			ClassQuery<Object> query = createQuery()
				.inPackage(PACKAGE_NAME);

			assertThat(query)
				.contains(Subject.OtherAnnotation.class, AnnotationFilter.class,
					ClassQueryTest.class, ClassQuery.class, Subject.class, SubjectReflection.class)
				.doesNotContain(Serializable.class, String.class, CtClass.class);
		}

		@Test
		void annotatedWith() {
			ClassQuery<Object> query = createQuery()
				.annotatedWith(Subject.Special.class);

			assertThat(query)
				.isSingleton(Subject.class)
				.doesNotContain(Serializable.class, AnnotationFilter.class, String.class, CtClass.class,
					ClassQueryTest.class, ClassQuery.class, SubjectReflection.class);
		}

		@Test
		void subtypeOf() {
			ClassQuery<Object> query = createQuery()
				.subtypeOf(AbstractQuery.class);

			assertThat(query)
				.contains(ClassQuery.class, MethodQuery.class)
				.doesNotContain(Serializable.class, AnnotationFilter.class, String.class, CtClass.class,
					ClassQueryTest.class, Subject.class, SubjectReflection.class);
		}

		@Test
		void filter() {
			ClassQuery<Object> query = createQuery()
				.filter(Class::isInterface);

			assertThat(query)
				.contains(AnnotationFilter.class)
				.doesNotContain(String.class, CtClass.class, ClassQueryTest.class,
					ClassQuery.class, Subject.class, SubjectReflection.class);
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
