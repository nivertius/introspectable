package org.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Predicate;

/**
 * Predicate-like interface for annotations on {@link AnnotatedElement}.
 */
@FunctionalInterface
public interface AnnotationFilter {
	/** Filter that matches any {@link AnnotatedElement element}. */
	AnnotationFilter ACCEPTING = AnnotationFilters.Accepting.INSTANCE;

	/** Filter that doesn't match any {@link AnnotatedElement element}. */
	AnnotationFilter REJECTING = AnnotationFilters.Rejecting.INSTANCE;

	/** Filter that matches only {@link AnnotatedElement elements} that doesnt have annotations. */
	AnnotationFilter ABSENT = AnnotationFilters.Absent.INSTANCE;

	/**
	 * Returns filter that requires specified annotation by type to be present on element to match.
	 *
	 * <p>Further filtering can be done with {@link Singular#andMatching}.
	 *
	 * @param annotationClass type of annotation to look for
	 * @param <A> annotation type
	 * @return filter for single annotation of speciefied type
	 */
	static <A extends Annotation> Singular<A> single(Class<A> annotationClass) {
		return AnnotationFilters.Single.create(annotationClass);
	}

	/**
	 * Function method that checks element.
	 *
	 * @param element element to test
	 * @return if element matches filter
	 */
	boolean matches(AnnotatedElement element);

	/**
	 * Creates conjunction between this and provided filter.
	 *
	 * @param other additional filter to be checked
	 * @return filter that matches only when this and other filter matches
	 */
	default AnnotationFilter and(AnnotationFilter other) {
		return AnnotationFilters.Conjunction.create(this, other);
	}

	/**
	 * Creates disjunction between this and provided filter.
	 *
	 * @param other additional filter to be checked
	 * @return filter that matches whenever this or other filter matches
	 */
	default AnnotationFilter or(AnnotationFilter other) {
		return AnnotationFilters.Disjunction.create(this, other);
	}

	/**
	 * Creates filter negation.
	 *
	 * @return filter that matches only when this filter doesn't
	 */
	default AnnotationFilter negated() {
		return new AnnotationFilters.Negated(this);
	}

	/**
	 * Concretization of filters that only match when there's a specified annotation present on element.
	 *
	 * @param <A> Annotation type to check.
	 */
	interface Singular<A extends Annotation> extends AnnotationFilter {
		/**
		 * Adds check on annotation that is present.
		 *
		 * @param addedPredicate predicate that must be matched on annotation for resulting filter to match
		 * @return filter that matches the same elements, but with additional check on specific annotation
		 */
		Singular<A> andMatching(Predicate<? super A> addedPredicate);
	}
}
