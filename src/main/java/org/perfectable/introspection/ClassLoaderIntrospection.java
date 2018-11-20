package org.perfectable.introspection;

public final class ClassLoaderIntrospection {
	private final ClassLoader classLoader;

	private ClassLoaderIntrospection(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public static ClassLoaderIntrospection of(ClassLoader classLoader) {
		return new ClassLoaderIntrospection(classLoader);
	}

	public Class<?> loadSafe(String className) {
		try {
			return classLoader.loadClass(className);
		}
		catch (ClassNotFoundException e) {
			throw new AssertionError(e);
		}
	}
}
