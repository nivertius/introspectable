package org.perfectable.introspection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class SimpleReflections {


	public static final Method OBJECT_EQUALS = getMethod(Object.class, "equals", Object.class);
	public static final Method OBJECT_HASH_CODE = getMethod(Object.class, "hashCode");

	public static final Method OBJECT_FINALIZE = getMethod(Object.class, "finalize");

	public static final Method OBJECT_NOTIFY = getMethod(Object.class, "notify");
	public static final Method OBJECT_NOTIFY_ALL = getMethod(Object.class, "notifyAll");
	// SUPPRESS NEXT 3 MultipleStringLiterals
	public static final Method OBJECT_WAIT = getMethod(Object.class, "wait");
	public static final Method OBJECT_WAIT_TIMEOUT = getMethod(Object.class, "wait", long.class);
	public static final Method OBJECT_WAIT_NANOSECONDS = getMethod(Object.class, "wait", long.class, int.class);

	public static final Method OBJECT_GET_CLASS = getMethod(Object.class, "getClass");
	public static final Method OBJECT_TO_STRING = getMethod(Object.class, "toString");
	public static final Method OBJECT_CLONE = getMethod(Object.class, "clone");

	public static final Method OBJECT_REGISTER_NATIVES = getMethod(Object.class, "registerNatives");


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
