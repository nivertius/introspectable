package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

interface Construction<T> {
	T construct();

	boolean matches(Class<?> type, Annotation... qualifiers);
}
