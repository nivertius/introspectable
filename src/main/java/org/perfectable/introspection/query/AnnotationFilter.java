package org.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Predicate;

@FunctionalInterface
public interface AnnotationFilter {
	AnnotationFilter ACCEPTING = AnnotationFilters.Accepting.INSTANCE;
	AnnotationFilter REJECTING = AnnotationFilters.Rejecting.INSTANCE;
	AnnotationFilter ABSENT = AnnotationFilters.Absent.INSTANCE;

	static <A extends Annotation> Singular<A> single(Class<A> annotationClass) {
		return AnnotationFilters.Single.create(annotationClass);
	}

	boolean matches(AnnotatedElement element);

	default AnnotationFilter and(AnnotationFilter other) {
		return AnnotationFilters.Conjunction.create(this, other);
	}

	default AnnotationFilter or(AnnotationFilter other) {
		return AnnotationFilters.Disjunction.create(this, other);
	}

	default AnnotationFilter negated() {
		return new AnnotationFilters.Negated(this);
	}

	interface Singular<A extends Annotation> extends AnnotationFilter {
		Singular<A> andMatching(Predicate<? super A> addedPredicate);
	}
}
