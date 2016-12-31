package org.perfectable.introspection;

import java.lang.reflect.Method;

public final class Methods {


	public static final Method OBJECT_EQUALS = get(Object.class, "equals", Object.class);
	public static final Method OBJECT_HASH_CODE = get(Object.class, "hashCode");

	public static final Method OBJECT_FINALIZE = get(Object.class, "finalize");

	public static final Method OBJECT_NOTIFY = get(Object.class, "notify");
	public static final Method OBJECT_NOTIFY_ALL = get(Object.class, "notifyAll");
	public static final Method OBJECT_WAIT = get(Object.class, "wait");
	public static final Method OBJECT_WAIT_TIMEOUT = get(Object.class, "wait", long.class);
	public static final Method OBJECT_WAIT_NANOSECONDS = get(Object.class, "wait", long.class, int.class);

	public static final Method OBJECT_GET_CLASS = get(Object.class, "getClass");
	public static final Method OBJECT_TO_STRING = get(Object.class, "toString");
	public static final Method OBJECT_CLONE = get(Object.class, "clone");

	public static final Method OBJECT_REGISTER_NATIVES = get(Object.class, "registerNatives");


	private Methods() {
		// utility
	}

	public static Method get(Class<?> declaringClass, String name, Class<?>... parameterTypes) {
		try {
			return declaringClass.getDeclaredMethod(name, parameterTypes);
		}
		catch (NoSuchMethodException | SecurityException e) {
			throw new AssertionError(e);
		}
	}
}
