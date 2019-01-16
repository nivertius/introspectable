package org.perfectable.introspection;

import org.perfectable.introspection.query.AnnotationQuery;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Entry point for method introspections.
 *
 * <p>Use {@link Introspections#introspect(Method)} to get instance of this class.
 */
public final class MethodIntrospection {
	static MethodIntrospection of(Method method) {
		return new MethodIntrospection(method);
	}

	/**
	 * Query for annotations on specified method.
	 *
	 * @return annotations query on introspected method.
	 */
	public AnnotationQuery<Annotation> annotations() {
		return AnnotationQuery.of(method);
	}

	/**
	 * Check if method is callable.
	 *
	 * <p>This only checks there is a scenario that introspected method can be called.
	 *
	 * @return if method is callable.
	 */
	public boolean isCallable() {
		return !Modifier.isAbstract(method.getModifiers());
	}

	private final Method method;

	private MethodIntrospection(Method method) {
		this.method = method;
	}
}
