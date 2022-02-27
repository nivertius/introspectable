package org.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Extension of {@link AbstractQuery} that matches {@link Member}, that are also {@link AnnotatedElement}.
 *
 * <p>Elements of this query have name, modifiers and possible annotations.
 *
 * <p>Double bound was selected to simplify selection, because all relevant members (field, method and constructor)
 * can be also annotated.
 *
 * @param <M> Type of member
 * @param <Q> Type of query that would be produced from restricting results
 */
abstract class MemberQuery<M extends @NonNull Member & @NonNull AnnotatedElement, Q extends MemberQuery<M, Q>>
		extends AbstractQuery<M, Q>
		implements Iterable<M> {

	/**
	 * Restricts query to members that have specified name, matched exactly.
	 *
	 * @param name name that member must have to match
	 * @return query that filters the same as this query, but returning elements that have specified name
	 */
	public abstract Q named(String name);

	/**
	 * Restricts query to members that have specified name, matched by pattern.
	 *
	 * @param namePattern pattern that member must match to be included in query
	 * @return query that filters the same as this query, but returning elements that have specified name
	 */
	public abstract Q nameMatching(Pattern namePattern);

	/**
	 * Restricts query to members that have an annotation of specified class.
	 *
	 * @param annotationClass class of annotation that member must have to match
	 * @return query that filters the same as this query, but returning elements that have specified annotation
	 */
	public Q annotatedWith(Class<? extends Annotation> annotationClass) {
		return annotatedWith(AnnotationFilter.single(annotationClass));
	}

	/**
	 * Restricts query to members that annotation filter matches.
	 *
	 * @param annotationFilter filter that must match
	 * @return query that filters the same as this query, but returning elements that only match specified filter
	 */
	public abstract Q annotatedWith(AnnotationFilter annotationFilter);

	/**
	 * Restricts query to members that have specified modifier on them.
	 *
	 * <p>Use {@link Modifier} to select modifiers.
	 *
	 * @param requiredModifier modifier bits that must be present on member
	 * @return query that filters the same as this query, but with modifiers that match provided
	 */
	public abstract Q requiringModifier(int requiredModifier);

	/**
	 * Restricts query to members that do not have specified modifier on them.
	 *
	 * <p>Use {@link Modifier} to select modifiers.
	 *
	 * @param excludedModifier modifier bits that must not be present on member
	 * @return query that filters the same as this query, but without modifiers that match provided
	 */
	public abstract Q excludingModifier(int excludedModifier);

	/**
	 * Returns query that provides members which have {@link java.lang.reflect.AccessibleObject#setAccessible}
	 * called on them.
	 *
	 * @return  query that filters the same as this query, but with accessible flag set
	 */
	public abstract Q asAccessible();
}
