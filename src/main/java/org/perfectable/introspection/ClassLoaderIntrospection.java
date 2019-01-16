package org.perfectable.introspection;

import org.perfectable.introspection.query.ClassQuery;

/**
 * Entry point for {@link ClassLoader} introspections.
 *
 * <p>Use {@link Introspections#introspect(ClassLoader)} to get instance of this class.
 */
public final class ClassLoaderIntrospection {
	static ClassLoaderIntrospection of(ClassLoader classLoader) {
		return new ClassLoaderIntrospection(classLoader);
	}

	/**
	 * Query for classes that introspected classloader provides.
	 *
	 * @return query for classes in classloader
	 */
	public ClassQuery<Object> classes() {
		return ClassQuery.of(classLoader);
	}

	/**
	 * Loads class assuming that it exists in classloader.
	 *
	 * <p>This asserts that class name is valid and classloader is able to load class with this name.
	 *
	 * <p>This method can be compared to {@link ClassLoader#loadClass(String)}, but with no compile-time exceptions.
	 *
	 * @param className fully qualified class name of class to load
	 * @return loaded class
	 * @throws AssertionError when class loading actually fails.
	 */
	public Class<?> loadSafe(String className) {
		try {
			return classLoader.loadClass(className);
		}
		catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}
	}

	private final ClassLoader classLoader;

	private ClassLoaderIntrospection(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
}
