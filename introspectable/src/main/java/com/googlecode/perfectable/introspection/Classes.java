package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.function.Function;

import com.google.common.base.Throwables;

public final class Classes {
	public static boolean instantiable(Class<?> type) {
		return !type.isInterface(); // MARK abstract class, primitive, array...
	}

	public interface ClassLoaderFunction<T> extends Function<String, Class<? extends T>> {
		// renaming only
	}

	public static <T> ClassLoaderFunction<T> loaderFunction(final Class<T> expectedSupertype) {
		return new ClassLoaderFunction<T>() {

			@Override
			public Class<? extends T> apply(String input) {
				try {
					return load(input, expectedSupertype);
				}
				catch(ClassNotFoundException e) {
					throw Throwables.propagate(e);
				}
			}

		};
	}

	public static <T> T instantiate(Class<? extends T> type) {
		checkArgument(instantiable(type), "%s is not instantiable", type);
		try {
			T instance = type.newInstance();
			return instance;
		}
		catch(InstantiationException | IllegalAccessException e) {
			throw Throwables.propagate(e);
		}
	}

	public static <T> T instantiate(String className, Class<T> expectedSuperclass) {
		Class<?> loadedClass;
		try {
			loadedClass = load(className);
		}
		catch(ClassNotFoundException e) {
			throw Throwables.propagate(e);
		}
		Class<? extends T> instanceClass = loadedClass.asSubclass(expectedSuperclass);
		return instantiate(instanceClass);
	}

	public static <T> Class<? extends T> load(String className, Class<T> expectedSupertype)
			throws ClassNotFoundException, ClassCastException {
		Class<?> raw = load(className);
		final Class<? extends T> casted = raw.asSubclass(expectedSupertype);
		return casted;
	}

	public static Class<?> load(String className) throws ClassNotFoundException {
		return Thread.currentThread().getContextClassLoader().loadClass(className);
	}

	private Classes() {
	}

}
