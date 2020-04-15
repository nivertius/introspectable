package org.perfectable.introspection;

import org.perfectable.introspection.query.AnnotationQuery;
import org.perfectable.introspection.type.TypeView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Entry point for {@link Field} introspections.
 *
 * <p>Use {@link Introspections#introspect(Field)} to get instance of this class.
 */
public final class FieldIntrospection {
	static FieldIntrospection of(Field field) {
		return new FieldIntrospection(field);
	}

	/**
	 * Query for annotations on introspected field.
	 *
	 * @return query for annotations
	 */
	public AnnotationQuery<Annotation> annotations() {
		return AnnotationQuery.of(field);
	}

	/**
	 * Extracts actual declared type of field.
	 *
	 * @return TypeView of field generic type
	 */
	public TypeView typeView() {
		return TypeView.ofTypeOf(field);
	}

	private final Field field;

	private FieldIntrospection(Field field) {
		this.field = field;
	}
}
