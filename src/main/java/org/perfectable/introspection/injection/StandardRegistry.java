package org.perfectable.introspection.injection;

import org.perfectable.introspection.query.AnnotationQuery;
import org.perfectable.introspection.query.ConstructorQuery;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.perfectable.introspection.Introspections.introspect;

public class StandardRegistry implements Registry {
	private final Set<RegisteredSingleton<?>> singletons = new HashSet<>();

	public static StandardRegistry create() {
		return new StandardRegistry();
	}

	public StandardRegistry registerSingleton(Object singleton) {
		RegisteredSingleton<?> registeredSingleton = RegisteredSingleton.create(singleton);
		singletons.add(registeredSingleton);
		return this;
	}

	@Override
	public <T> T fetch(Class<T> targetClass, Annotation... qualifiers) {
		for (RegisteredSingleton<?> singleton : singletons) {
			if (singleton.matches(targetClass, qualifiers)) {
				@SuppressWarnings("unchecked")
				T casted = (T) singleton.asInjectable();
				return casted;
			}
		}
		Constructor<T> constructor;
		try {
			constructor = findInjectableConstructor(targetClass);
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e); // SUPPRESS AvoidThrowingRawExceptionTypes
		}
		Object[] arguments = prepareArguments(constructor);
		T instance;
		try {
			instance = constructor.newInstance(arguments);
		}
		catch (IllegalAccessException | InstantiationException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e); // SUPPRESS AvoidThrowingRawExceptionTypes
		}
		injectFields(instance);
		injectMethods(instance);
		if (targetClass.isAnnotationPresent(Singleton.class)) {
			registerSingleton(instance);
		}
		return instance;
	}

	private static <T> Constructor<T> findInjectableConstructor(Class<T> targetClass) throws NoSuchMethodException {
		return ConstructorQuery.of(targetClass)
				.annotatedWith(Inject.class)
				.stream()
				.peek(constructor -> constructor.setAccessible(true))
				.findFirst()
				.orElseGet(() -> introspect(targetClass).defaultConstructor());

	}

	private void injectFields(Object targetObject) {
		introspect(targetObject.getClass())
				.fields()
				.annotatedWith(Inject.class)
				.stream()
				.peek(field -> field.setAccessible(true))
				.forEach(field -> injectField(field, targetObject));
	}

	private void injectField(Field field, Object targetObject) {
		Annotation[] annotations = findQualifiers(field);
		Object injection = fetch(field.getType(), annotations);
		try {
			field.set(targetObject, injection);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e); // SUPPRESS AvoidThrowingRawExceptionTypes
		}
	}


	private void injectMethods(Object targetObject) {
		introspect(targetObject.getClass())
				.methods()
				.annotatedWith(Inject.class)
				.stream()
				.peek(method -> method.setAccessible(true))
				.forEach(method -> injectMethod(method, targetObject));
	}

	private void injectMethod(Method method, Object targetObject) {
		Object[] arguments = prepareArguments(method);
		try {
			method.invoke(targetObject, arguments);
		}
		catch (IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e); // SUPPRESS AvoidThrowingRawExceptionTypes
		}
	}

	@Nonnull
	private <T> Object[] prepareArguments(Executable executable) {
		Object[] arguments = new Object[executable.getParameterCount()];
		int counter = 0;
		for (Parameter parameter : executable.getParameters()) {
			Annotation[] qualifiers = findQualifiers(parameter);
			arguments[counter] = fetch(parameter.getType(), qualifiers);
		}
		return arguments;
	}


	private Annotation[] findQualifiers(AnnotatedElement element) {
		return AnnotationQuery.of(element)
				.annotatedWith(Qualifier.class)
				.stream().toArray(Annotation[]::new);
	}

	private static class RegisteredSingleton<T> {
		private final T singleton;

		RegisteredSingleton(T singleton) {
			this.singleton = singleton;
		}

		public static <T> RegisteredSingleton<T> create(T singleton) {
			checkNotNull(singleton);
			return new RegisteredSingleton<>(singleton);
		}

		public T asInjectable() {
			return singleton;
		}

		public boolean matches(Class<?> type, Annotation... qualifiers) {
			if (!type.isInstance(singleton)) {
				return false;
			}
			ImmutableList<Annotation> annotationsList = ImmutableList.copyOf(qualifiers);
			if (!introspect(singleton.getClass()).annotations() // SUPPRESSS SimplifyBooleanReturns
					.stream().allMatch(annotationsList::contains)) {


				return false;
			}
			return true;
		}
	}
}
