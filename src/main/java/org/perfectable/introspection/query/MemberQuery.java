package org.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;

public abstract class MemberQuery<M extends Member & AnnotatedElement, Q extends MemberQuery<M, ? extends Q>>
		extends AbstractQuery<M, Q>
		implements Iterable<M> {

	public abstract Q named(String name);

	public abstract Q typed(Class<?> type);

	public Q annotatedWith(Class<? extends Annotation> annotationClass) {
		return annotatedWith(AnnotationFilter.of(annotationClass));
	}

	public abstract Q annotatedWith(AnnotationFilter annotationFilter);

	public abstract Q excludingModifier(int excludedModifier);

	public abstract Q asAccessible();

	MemberQuery() {
		// package extension only
	}
}
