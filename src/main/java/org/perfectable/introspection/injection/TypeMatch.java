package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

interface TypeMatch {

	boolean matches(Class<?> type, Annotation... qualifiers);

}
