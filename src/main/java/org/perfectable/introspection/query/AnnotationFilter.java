package org.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.function.Predicate;

@FunctionalInterface
public interface AnnotationFilter {

	static <A extends Annotation> SingleAnnotationFilter<A> of(Class<A> annotationClass) {
		return SingleAnnotationFilter.create(annotationClass);
	}

	boolean matches(AnnotatedElement element);

	final class SingleAnnotationFilter<A extends Annotation> implements AnnotationFilter {

		private final Class<A> annotationClass;
		private final Predicate<A> predicate;

		public static <A extends Annotation> SingleAnnotationFilter<A> create(Class<A> annotationClass) {
			return new SingleAnnotationFilter<>(annotationClass, Objects::nonNull);
		}

		private SingleAnnotationFilter(Class<A> annotationClass, Predicate<A> predicate) {
			this.annotationClass = annotationClass;
			this.predicate = predicate;
		}

		public SingleAnnotationFilter<A> andMatching(Predicate<A> addedPredicate) {
			return new SingleAnnotationFilter<>(this.annotationClass, this.predicate.and(addedPredicate));
		}

		@Override
		public boolean matches(AnnotatedElement element) {
			A annotation = element.getAnnotation(this.annotationClass);
			return this.predicate.test(annotation);
		}

	}
}
