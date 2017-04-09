package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

interface Construction<T> {
	T create(Class<T> targetClass);

	boolean matches(Class<?> type, Annotation... qualifiers);
}
