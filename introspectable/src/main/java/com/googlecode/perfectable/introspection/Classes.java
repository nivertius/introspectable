package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.common.base.Throwables;

public final class Classes {
	public static boolean instantiable(Class<?> type) {
		return !type.isInterface(); // MARK abstract class, primitive, array...
	}

	public static <T> T instantiate(Class<T> type) {
		checkState(instantiable(type));
		try {
			T instance = type.newInstance();
			return instance;
		}
		catch(InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e); // TODO Auto-generated catch block
		}
	}

	public static <T> T instantiate(String className, Class<T> expectedSuperclass) {
		Class<?> loadedClass;
		try {
			loadedClass = load(className);
		}
		catch(ClassNotFoundException e) {
			throw Throwables.propagate(e); // TODO Auto-generated catch block
		}
		Class<? extends T> instanceClass = loadedClass.asSubclass(expectedSuperclass);
		return instantiate(instanceClass);
	}

	public static Class<?> load(String className) throws ClassNotFoundException {
		return Thread.currentThread().getContextClassLoader().loadClass(className);
	}

	// MARK this is overly simplified, should return set of bounds, needs mooore checks
	public static <T> Class<?> getInterfaceArgumentBound(Class<? extends T> testedClass, Class<T> interfaceClass,
			int argumentPosition) {
		for(Type genericInterface : testedClass.getGenericInterfaces()) {
			if(!(genericInterface instanceof ParameterizedType)) {
				continue;
			}
			ParameterizedType parametrized = (ParameterizedType) genericInterface;
			if(!interfaceClass.equals(parametrized.getRawType())) {
				continue;
			}
			Type parameter = parametrized.getActualTypeArguments()[argumentPosition];
			if(!(parameter instanceof Class)) {
				// MARK support TypeVariable
				throw new RuntimeException(parameter.getClass().getName());
			}
			Class<?> parameterClass = (Class<?>) parameter;
			return parameterClass;
		}
		throw new IllegalArgumentException("Class " + testedClass + " has no generic interface " + interfaceClass);
	}

	private Classes() {
	}

}
