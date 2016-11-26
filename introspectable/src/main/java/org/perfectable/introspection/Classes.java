package org.perfectable.introspection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public final class Classes {
	public static boolean instantiable(Class<?> type) {
		// SUPPRESS NEXT BooleanExpressionComplexity
		return !type.isInterface()
				&& !type.isArray()
				&& (type.getModifiers() & Modifier.ABSTRACT) == 0
				&& !type.isPrimitive();
	}

	@FunctionalInterface
	public interface ClassLoaderFunction<T> extends Function<String, Class<? extends T>> {
		// renaming only
	}

	public static <T> ClassLoaderFunction<T> loaderFunction(Class<T> expectedSupertype) {
		return input -> {
			checkNotNull(input);
			try {
				return load(input, expectedSupertype);
			}
			catch (ClassNotFoundException e) {
				throw new RuntimeException(e); // SUPPRESS no better exception here
			}
		};
	}

	public static <T> T instantiate(Class<? extends T> type) {
		checkArgument(instantiable(type), "%s is not instantiable", type);
		try {
			T instance = type.getConstructor().newInstance();
			return instance;
		}
		catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			throw new RuntimeException(e); // SUPPRESS no better exception here
		}
	}

	public static <T> T instantiate(String className, Class<T> expectedSuperclass) {
		Class<? extends T> instanceClass;
		try {
			instanceClass = load(className, expectedSuperclass);
		}
		catch (ClassNotFoundException e) {
			throw new RuntimeException(e); // SUPPRESS no better exception here
		}
		return instantiate(instanceClass);
	}

	public static <T> Class<? extends T> load(String className, Class<T> expectedSupertype)
			throws ClassNotFoundException, ClassCastException {
		Class<?> raw = load(className);
		Class<? extends T> casted = raw.asSubclass(expectedSupertype);
		return casted;
	}

	public static Class<?> load(String className) throws ClassNotFoundException {
		return Thread.currentThread().getContextClassLoader().loadClass(className);
	}

	private Classes() {
	}

}
