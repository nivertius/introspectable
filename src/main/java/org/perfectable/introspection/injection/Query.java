package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import java.util.Set;

public interface Query<T> {
	static <T> Query<T> typed(Class<T> targetClass) {
		return StandardQuery.create(targetClass);
	}

	Query<T> qualifiedWith(Annotation qualifier);

	Query<T> qualifiedWith(Class<? extends Annotation> qualifierType);

	boolean matches(Class<?> targetClass, Set<Annotation> annotations);
}
