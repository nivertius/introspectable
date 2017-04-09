package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

public interface Registry {

	<T> T fetch(Class<T> targetClass, Annotation... qualifiers);
}
