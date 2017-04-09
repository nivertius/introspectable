package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

@FunctionalInterface
interface TypeMatch {

	boolean matches(Class<?> type, Annotation... qualifiers);

}
