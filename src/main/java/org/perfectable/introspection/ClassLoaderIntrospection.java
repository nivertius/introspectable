package org.perfectable.introspection;

import org.perfectable.introspection.query.ClassQuery;

public final class ClassLoaderIntrospection {
	private final ClassLoader classLoader;

	private ClassLoaderIntrospection(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public static ClassLoaderIntrospection of(ClassLoader classLoader) {
		return new ClassLoaderIntrospection(classLoader);
	}

	public ClassQuery<Object> classes() {
		return ClassQuery.of(classLoader);
	}
}