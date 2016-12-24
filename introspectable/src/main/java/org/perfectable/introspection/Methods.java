package org.perfectable.introspection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class Methods {

	public static final Method OBJECT_EQUALS = safeExtract(Object.class, "equals", Object.class);
	public static final Method OBJECT_TO_STRING = safeExtract(Object.class, "toString");
	public static final Method OBJECT_FINALIZE = safeExtract(Object.class, "finalize");

	public static Method safeExtract(Class<?> declaringClass, String name, Class<?>... parameterTypes)
			throws AssertionError {
		try {
			return declaringClass.getDeclaredMethod(name, parameterTypes);
		}
		catch (NoSuchMethodException e) {
			throw new AssertionError("Method which is expected to exist is missing", e);
		}
	}

	public static boolean isCallable(Method method) {
		return method.isAccessible() && !Modifier.isAbstract(method.getModifiers());
	}

	private Methods() {
	}

}
