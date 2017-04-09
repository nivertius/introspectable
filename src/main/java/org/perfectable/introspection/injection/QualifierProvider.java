package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

import javax.inject.Qualifier;

import static org.perfectable.introspection.Introspections.introspect;

public class QualifierProvider {
	public Annotation[] get() {
		return introspect(getClass()).annotations().annotatedWith(Qualifier.class)
				.stream().toArray(Annotation[]::new);
	}
}
