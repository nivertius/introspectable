package org.perfectable.introspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class SimpleReflections {

	private SimpleReflections() {
		// utility
	}

	public static Field getField(Class<?> declaringClass, String name) {
		try {
			return declaringClass.getDeclaredField(name);
		}
		catch (NoSuchFieldException | SecurityException e) {
			throw new AssertionError(e);
		}
	}

	public static <T> Constructor<T> getConstructor(Class<T> declaringClass, Class<?>... parameterTypes) {
		try {
			return declaringClass.getDeclaredConstructor(parameterTypes);
		}
		catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		}
	}

	public static Method getMethod(Class<?> declaringClass, String name, Class<?>... parameterTypes) {
		try {
			return declaringClass.getDeclaredMethod(name, parameterTypes);
		}
		catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		}
	}
}
