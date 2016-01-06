package com.googlecode.perfectable.introspection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

public final class Methods {
	
	public static final Method OBJECT_EQUALS = extractObjectMethod("equals", Object.class);
	public static final Method OBJECT_TO_STRING = extractObjectMethod("toString");
	public static final Method OBJECT_FINALIZE = extractObjectMethod("finalize");
	
	public static Optional<Method> similar(Class<?> sourceClass, Method otherClassMethod) {
		final String methodName = otherClassMethod.getName();
		final Class<?>[] methodParameterTypes = otherClassMethod.getParameterTypes();
		if(methodName == null || methodParameterTypes == null) {
			throw new IllegalArgumentException();
		}
		return find(sourceClass, methodName, methodParameterTypes);
	}
	
	@Deprecated
	public static Optional<Method> find(Class<?> sourceClass, String name, Class<?>... parameterTypes) {
		return Introspection.of(sourceClass).methods().named(name).parameters(parameterTypes).option();
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
		final boolean actuallyReturns = !Void.TYPE.equals(method.getReturnType());
		final boolean hasNoParameters = method.getParameterTypes().length == 0;
		final boolean startsWithAppropriatePrefix =
				Boolean.class.equals(method.getReturnType()) && method.getName().startsWith("is") ||
						!Boolean.class.equals(method.getReturnType()) && method.getName().startsWith("get");
		return actuallyReturns && hasNoParameters && startsWithAppropriatePrefix;
	}
	
	public static boolean isSetter(Method method) {
		final boolean doesntReturn = Void.TYPE.equals(method.getReturnType());
		final boolean hasOneParameter = method.getParameterTypes().length == 1;
		final boolean startsWithAppropriatePrefix = method.getName().startsWith("set");
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
	
	private static Method extractObjectMethod(String name, Class<?>... parameterTypes) {
		try {
			return Object.class.getDeclaredMethod(name, parameterTypes);
		}
		catch(NoSuchMethodException | SecurityException e) {
			throw new AssertionError("Object is missing standard method", e);
		}
	}
	
	private Methods() {
	}
	
}
