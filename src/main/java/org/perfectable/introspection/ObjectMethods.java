package org.perfectable.introspection;

import java.lang.reflect.Method;

/**
 * Convenient holder for reflections of methods in {@link Object}.
 */
public final class ObjectMethods {
	/** Reflection for {@link Object#equals(Object)}. */
	public static final Method EQUALS = getMethod(Object.class, "equals", Object.class);
	/** Reflection for {@link Object#hashCode()}. */
	public static final Method HASH_CODE = getMethod(Object.class, "hashCode");

	/** Reflection for {@link Object#finalize()}. */
	public static final Method FINALIZE = getMethod(Object.class, "finalize");

	/** Reflection for {@link Object#notify()}. */
	public static final Method NOTIFY = getMethod(Object.class, "notify");
	/** Reflection for {@link Object#notifyAll()}. */
	public static final Method NOTIFY_ALL = getMethod(Object.class, "notifyAll");

	// SUPPRESS NEXT 6 MultipleStringLiterals
	/** Reflection for {@link Object#wait()}. */
	public static final Method WAIT = getMethod(Object.class, "wait");
	/** Reflection for {@link Object#wait(long)}. */
	public static final Method WAIT_TIMEOUT = getMethod(Object.class, "wait", long.class);
	/** Reflection for {@link Object#wait(long, int)}. */
	public static final Method WAIT_NANOSECONDS = getMethod(Object.class, "wait", long.class, int.class);

	/** Reflection for {@link Object#getClass()}. */
	public static final Method GET_CLASS = getMethod(Object.class, "getClass");
	/** Reflection for {@link Object#toString()}. */
	public static final Method TO_STRING = getMethod(Object.class, "toString");
	/** Reflection for {@link Object#clone()}. */
	public static final Method CLONE = getMethod(Object.class, "clone");

	/** Reflection for {@link Object#registerNatives()}. */
	public static final Method REGISTER_NATIVES = getMethod(Object.class, "registerNatives");

	private ObjectMethods() {
		// utility
	}

	private static Method getMethod(Class<?> declaringClass, String name, Class<?>... parameterTypes) {
		try {
			return declaringClass.getDeclaredMethod(name, parameterTypes);
		}
		catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		}
	}
}
