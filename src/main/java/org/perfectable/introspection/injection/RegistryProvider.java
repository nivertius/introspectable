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
import java.util.stream.Stream;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Qualifier;

import static org.perfectable.introspection.Introspections.introspect;

final class RegistryProvider<T> implements Provider<T> {
	private final Class<T> createdClass;
	private final Registry registry;

	private RegistryProvider(Class<T> createdClass, Registry registry) {
		this.createdClass = createdClass;
		this.registry = registry;
	}

	public static <T> RegistryProvider<T> of(Class<T> createdClass, Registry registry) {
		return new RegistryProvider<T>(createdClass, registry);
	}

	@Override
	public T get() {
		Constructor<T> constructor;
		try {
			constructor = findInjectableConstructor(createdClass);
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e); // SUPPRESS AvoidThrowingRawExceptionTypes
		}
		Object[] arguments = prepareArguments(constructor, registry);
		T instance;
		try {
			instance = constructor.newInstance(arguments);
		}
		catch (IllegalAccessException | InstantiationException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e); // SUPPRESS AvoidThrowingRawExceptionTypes
		}
		Stream.Builder<Injection> injections = Stream.builder();
		fieldInjections(instance.getClass()).forEach(injections::add);
		methodInjections(instance.getClass()).forEach(injections::add);
		injections.build()
				.forEach(injection -> injection.perform(instance));
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

	private Stream<Injection> fieldInjections(Class<?> targetObjectClass) {
		return introspect(targetObjectClass)
				.fields()
				.annotatedWith(Inject.class)
				.stream()
				.peek(field -> field.setAccessible(true))
				.map(field -> FieldInjection.create(field, registry));
	}


	private Stream<Injection> methodInjections(Class<?> targetObjectClass) {
		return introspect(targetObjectClass)
				.methods()
				.annotatedWith(Inject.class)
				.stream()
				.peek(method -> method.setAccessible(true))
				.map(method -> MethodInjection.create(method, registry));
	}

	private static Object[] prepareArguments(Executable executable, Registry registry) {
		Object[] arguments = new Object[executable.getParameterCount()];
		int counter = 0;
		for (Parameter parameter : executable.getParameters()) {
			Annotation[] qualifiers = findQualifiers(parameter);
			Class<?> type = parameter.getType();
			Object argument = registry.fetch(type, qualifiers);
			arguments[counter] = argument;
			counter++;
		}
		return arguments;
	}


	static Annotation[] findQualifiers(AnnotatedElement element) {
		return AnnotationQuery.of(element)
				.annotatedWith(Qualifier.class)
				.stream().toArray(Annotation[]::new);
	}

	@FunctionalInterface
	private interface Injection {
		void perform(Object targetObject);
	}

	private static final class FieldInjection implements Injection {
		private final Field field;
		private final Registry registry;

		private FieldInjection(Field field, Registry registry) {
			this.field = field;
			this.registry = registry;
		}

		public static FieldInjection create(Field field, Registry registry) {
			return new FieldInjection(field, registry);
		}

		@Override
		public void perform(Object targetObject) {
			Class<?> fieldType = field.getType();
			Annotation[] qualifiers = findQualifiers(field);
			Object injection = registry.fetch(fieldType, qualifiers);
			try {
				field.set(targetObject, injection);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e); // SUPPRESS AvoidThrowingRawExceptionTypes
			}
		}
	}

	private static final class MethodInjection implements Injection {
		private final Registry registry;
		private final Method method;

		static MethodInjection create(Method method, Registry registry) {
			return new MethodInjection(method, registry);
		}

		private MethodInjection(Method method, Registry registry) {
			this.method = method;
			this.registry = registry;
		}

		@Override
		public void perform(Object targetObject) {
			Object[] arguments = prepareArguments(method, registry);
			try {
				method.invoke(targetObject, arguments);
			}
			catch (IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e); // SUPPRESS AvoidThrowingRawExceptionTypes
			}
		}
	}
}
