package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.google.common.base.Optional;
import com.google.common.base.Throwables;

public final class Methods {

	public static Optional<Method> similar(Class<?> sourceClass, Method otherClassMethod) {
		final String methodName = otherClassMethod.getName();
		final Class<?>[] methodParameterTypes = otherClassMethod.getParameterTypes();
		if(methodName == null || methodParameterTypes == null) {
			throw new IllegalArgumentException();
		}
		return find(sourceClass, methodName, methodParameterTypes);
	}

	public static Optional<Method> find(Class<?> sourceClass, String name, Class<?>... parameterTypes) {
		for(Class<?> currentClass : InheritanceChain.startingAt(sourceClass)) {
			try {
				final Method method = currentClass.getDeclaredMethod(name, parameterTypes);
				return Optional.of(method);
			}
			catch(NoSuchMethodException e) {
				// continue the search
			}
			catch(SecurityException e) {
				throw Throwables.propagate(e);
			}
		}
		return Optional.absent();
	}

	public static Optional<Method> findGetter(Class<?> beanClass, String name, Class<?> type) {
		Optional<Method> getter = find(beanClass, getterName(name));
		if(!getter.isPresent()) {
			return getter;
		}
		checkState(getter.get().getReturnType().equals(type));
		return getter;
	}

	public static Optional<Method> findSetter(Class<?> beanClass, String name, Class<?> type) {
		Optional<Method> setter = find(beanClass, setterName(name), type);
		if(!setter.isPresent()) {
			return setter;
		}
		checkState(setter.get().getReturnType().equals(Void.TYPE));
		return setter;
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

	private Methods() {
	}

}
