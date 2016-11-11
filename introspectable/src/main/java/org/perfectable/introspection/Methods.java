package org.perfectable.introspection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public final class Methods {
	
	public static final Method OBJECT_EQUALS = safeExtract(Object.class, "equals", Object.class);
	public static final Method OBJECT_TO_STRING = safeExtract(Object.class, "toString");
	public static final Method OBJECT_FINALIZE = safeExtract(Object.class, "finalize");
	
	public static Optional<Method> similar(Class<?> sourceClass, Method otherClassMethod) {
		checkArgument(sourceClass != null);
		checkArgument(otherClassMethod != null);
		String methodName = otherClassMethod.getName();
		Class<?>[] methodParameterTypes = otherClassMethod.getParameterTypes();
		return Introspection.of(sourceClass).methods()
				.named(methodName).parameters(methodParameterTypes)
				.option();
	}
	
	public static Method safeExtract(Class<?> declaringClass, String name, Class<?>... parameterTypes)
			throws AssertionError {
		try {
			return declaringClass.getDeclaredMethod(name, parameterTypes);
		}
		catch(NoSuchMethodException e) {
			throw new AssertionError("Method which is expected to exist is missing", e);
		}
	}
	
	public static Optional<Method> findGetter(Class<?> beanClass, String name, Class<?> type) {
		return Introspection.of(beanClass).methods().named(getterName(name)).parameters().returning(type).option();
	}
	
	public static Optional<Method> findSetter(Class<?> beanClass, String name, Class<?> type) {
		return Introspection.of(beanClass).methods().named(setterName(name)).parameters(type).returningVoid().option();
	}
	
	public static boolean isCallable(Method method) {
		return method.isAccessible() && !Modifier.isAbstract(method.getModifiers());
	}
	
	public static boolean isGetter(Method method) {
		boolean actuallyReturns = !Void.TYPE.equals(method.getReturnType());
		boolean hasNoParameters = method.getParameterTypes().length == 0;
		String appropriatePrefix = Boolean.class.equals(method.getReturnType()) ? "is" : "get";
		boolean startsWithAppropriatePrefix = method.getName().startsWith(appropriatePrefix);
		return actuallyReturns && hasNoParameters && startsWithAppropriatePrefix;
	}
	
	public static boolean isSetter(Method method) {
		boolean doesntReturn = Void.TYPE.equals(method.getReturnType());
		boolean hasOneParameter = method.getParameterTypes().length == 1;
		boolean startsWithAppropriatePrefix = method.getName().startsWith("set");
		return doesntReturn && hasOneParameter && startsWithAppropriatePrefix;
	}
	
	private static String getterName(String name) {
		return capitalizeWithPrefix("get", name);
	}
	
	private static String setterName(String name) {
		return capitalizeWithPrefix("set", name);
	}
	
	private static String capitalizeWithPrefix(String prefix, String name) {
		return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
	}
	
	private Methods() {
	}
	
}
