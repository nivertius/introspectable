package com.googlecode.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Predicate;

public interface AnnotationFilter {
	
	final class SingleAnnotationFilter<A extends Annotation> implements AnnotationFilter {
		
		private final Class<A> annotationClass;
		private final Predicate<A> predicate;

		public static <A extends Annotation> SingleAnnotationFilter<A> create(Class<A> annotationClass) {
			return new SingleAnnotationFilter<>(annotationClass, annotation -> annotation != null);
		}
		
		private SingleAnnotationFilter(Class<A> annotationClass, Predicate<A> predicate) {
			this.annotationClass = annotationClass;
			this.predicate = predicate;
		}

		public SingleAnnotationFilter<A> andMatching(Predicate<A> addedPredicate) {
			return new SingleAnnotationFilter<>(this.annotationClass, this.predicate.and(addedPredicate));
		}
		
		@Override
		public boolean appliesOn(AnnotatedElement element) {
			A annotation = element.getAnnotation(this.annotationClass);
			return this.predicate.test(annotation);
		}
		
	}
	
	static <A extends Annotation> SingleAnnotationFilter<A> of(Class<A> annotationClass) {
		return SingleAnnotationFilter.create(annotationClass);
	}
	
	boolean appliesOn(AnnotatedElement element);
}
