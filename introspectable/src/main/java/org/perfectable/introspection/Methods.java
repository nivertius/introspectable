package org.perfectable.introspection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public final class Methods {

	public static final Method OBJECT_EQUALS = safeExtract(Object.class, "equals", Object.class);
	public static final Method OBJECT_TO_STRING = safeExtract(Object.class, "toString");
	public static final Method OBJECT_FINALIZE = safeExtract(Object.class, "finalize");

	private static final String BOOLEAN_GETTER_PREFIX = "is";
	private static final String STANDARD_GETTER_PREFIX = "get";
	private static final String SETTER_PREFIX = "set";

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
		catch (NoSuchMethodException e) {
			throw new AssertionError("Method which is expected to exist is missing", e);
		}
	}

	public static Optional<Method> findGetter(Class<?> beanClass, String name, Class<?> type) {
		return Introspection.of(beanClass).methods()
				.named(getterName(name, type))
				.parameters()
				.returning(type)
				.option();
	}

	public static Optional<Method> findSetter(Class<?> beanClass, String name, Class<?> type) {
		return Introspection.of(beanClass).methods()
				.named(setterName(name))
				.parameters(type)
				.returningVoid()
				.option();
	}

	public static boolean isCallable(Method method) {
		return method.isAccessible() && !Modifier.isAbstract(method.getModifiers());
	}

	public static boolean isGetter(Method method) {
		boolean actuallyReturns = !Void.TYPE.equals(method.getReturnType());
		boolean hasNoParameters = method.getParameterTypes().length == 0;
		String appropriatePrefix = getterPrefix(method.getReturnType());
		boolean startsWithAppropriatePrefix = method.getName().startsWith(appropriatePrefix);
		return actuallyReturns && hasNoParameters && startsWithAppropriatePrefix;
	}

	private static String getterPrefix(Class<?> returnType) {
		boolean returnsBoolean = Boolean.class.equals(returnType)
				|| boolean.class.equals(returnType);
		return returnsBoolean ? BOOLEAN_GETTER_PREFIX : STANDARD_GETTER_PREFIX;
	}

	public static boolean isSetter(Method method) {
		boolean doesntReturn = Void.TYPE.equals(method.getReturnType());
		boolean hasOneParameter = method.getParameterTypes().length == 1;
		boolean startsWithAppropriatePrefix = method.getName().startsWith(SETTER_PREFIX);
		return doesntReturn && hasOneParameter && startsWithAppropriatePrefix;
	}

	public static String propertyNameFromGetter(Method getter) {
		checkArgument(isGetter(getter));
		String unformatted = getter.getName();
		int prefixLength = getterPrefix(getter.getReturnType()).length();
		return String.valueOf(unformatted.charAt(prefixLength)).toLowerCase()
				+ unformatted.substring(prefixLength + 1);
	}

	public static String propertyNameFromSetter(Method setter) {
		checkArgument(isGetter(setter));
		String unformatted = setter.getName();
		int prefixLength = SETTER_PREFIX.length();
		return String.valueOf(unformatted.charAt(prefixLength)).toLowerCase()
				+ unformatted.substring(prefixLength + 1);
	}

	private static String getterName(String name, Class<?> type) {
		String prefix = getterPrefix(type);
		return capitalizeWithPrefix(prefix, name);
	}

	private static String capitalizeWithPrefix(String prefix, String name) {
		return prefix + name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	private static String setterName(String name) {
		return capitalizeWithPrefix(SETTER_PREFIX, name);
	}

	private Methods() {
	}

}
