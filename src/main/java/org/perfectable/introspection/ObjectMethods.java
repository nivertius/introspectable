package org.perfectable.introspection;

import java.lang.reflect.Method;

public final class ObjectMethods {
	public static final Method EQUALS = getMethod(Object.class, "equals", Object.class);
	public static final Method HASH_CODE = getMethod(Object.class, "hashCode");

	public static final Method FINALIZE = getMethod(Object.class, "finalize");

	public static final Method NOTIFY = getMethod(Object.class, "notify");
	public static final Method NOTIFY_ALL = getMethod(Object.class, "notifyAll");
	// SUPPRESS NEXT 3 MultipleStringLiterals
	public static final Method WAIT = getMethod(Object.class, "wait");
	public static final Method WAIT_TIMEOUT = getMethod(Object.class, "wait", long.class);
	public static final Method WAIT_NANOSECONDS = getMethod(Object.class, "wait", long.class, int.class);

	public static final Method GET_CLASS = getMethod(Object.class, "getClass");
	public static final Method TO_STRING = getMethod(Object.class, "toString");
	public static final Method CLONE = getMethod(Object.class, "clone");

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
