package org.perfectable.introspection;

import org.perfectable.introspection.query.ClassQuery;

import javax.annotation.Nullable;

/**
 * Entry point for {@link ClassLoader} introspections.
 *
 * <p>Use {@link Introspections#introspect(ClassLoader)} to get instance of this class.
 */
public abstract class ClassLoaderIntrospection {
	/** Introspection of System ClassLoader. */
	public static final ClassLoaderIntrospection SYSTEM =
		new Standard(ClassLoader.getSystemClassLoader());

	static ClassLoaderIntrospection of(@Nullable ClassLoader classLoader) {
		if (classLoader == null) {
			return Bootstrap.INSTANCE;
		}
		else {
			return new Standard(classLoader);
		}
	}

	/**
	 * Query for classes that introspected classloader provides.
	 *
	 * @return query for classes in classloader
	 */
	public abstract ClassQuery<Object> classes();

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
	public abstract Class<?> loadSafe(String className);

	ClassLoaderIntrospection() {
		// package-only inheritance
	}

	private static final class Bootstrap extends ClassLoaderIntrospection {
		public static final Bootstrap INSTANCE = new Bootstrap();

		@Override
		public ClassQuery<Object> classes() {
			throw new IllegalStateException("Cannot list classes of bootstrap classloader");
		}

		@Override
		public Class<?> loadSafe(String className) {
			try {
				return Class.forName(className, /* initialize= */ true, null);
			}
			catch (ClassNotFoundException e) {
				throw new AssertionError(e);
			}
		}
	}

	private static final class Standard extends ClassLoaderIntrospection {
		private final ClassLoader classLoader;

		Standard(ClassLoader classLoader) {
			this.classLoader = classLoader;
		}

		@Override
		public ClassQuery<Object> classes() {
			return ClassQuery.of(classLoader);
		}

		@Override
		public Class<?> loadSafe(String className) {
			try {
				return classLoader.loadClass(className);
			}
			catch (ClassNotFoundException e) {
				throw new AssertionError(e);
			}
		}

	}

}
