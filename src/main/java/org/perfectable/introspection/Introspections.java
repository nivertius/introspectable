package org.perfectable.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Convenient entry point for introspections.
 *
 * <p>Use {@link #introspect} to start introspection for either supported type.
 */
public final class Introspections {

	/**
	 * Introspect a class.
	 *
	 * @param type class to introspect
	 * @param <X> class parameter
	 * @return class introspections
	 */
	public static <X> ClassIntrospection<X> introspect(Class<X> type) {
		return ClassIntrospection.of(type);
	}

	/**
	 * Introspect a method.
	 *
	 * @param method method to introspect
	 * @return method introspections
	 */
	public static MethodIntrospection introspect(Method method) {
		return MethodIntrospection.of(method);
	}

	/**
	 * Introspect a field.
	 *
	 * @param field field to introspect
	 * @return field introspections
	 */
	public static FieldIntrospection introspect(Field field) {
		return FieldIntrospection.of(field);
	}

	/**
	 * Introspect a classloader.
	 *
	 * @param classLoader class loader to introspect
	 * @return classloader introspections
	 */
	public static ClassLoaderIntrospection introspect(ClassLoader classLoader) {
		return ClassLoaderIntrospection.of(classLoader);
	}

	private Introspections() {
		// utility
	}
}
