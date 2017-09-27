package org.perfectable.introspection;

import org.perfectable.introspection.query.AnnotationQuery;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public final class FieldIntrospection {
	static FieldIntrospection of(Field field) {
		return new FieldIntrospection(field);
	}

	public AnnotationQuery<Annotation> annotations() {
		return AnnotationQuery.of(field);
	}

	private final Field field;

	private FieldIntrospection(Field field) {
		this.field = field;
	}
}
