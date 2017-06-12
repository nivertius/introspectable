package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

final class SingletonRegistration<T> implements Registration<T> {
	private final T singleton;
	private final TypeMatch typeMatch;

	public static <T> SingletonRegistration<T> create(T singleton) {
		TypeMatch typeMatch = TypeMatch.create(singleton.getClass());
		return new SingletonRegistration<>(singleton, typeMatch);
	}

	private SingletonRegistration(T singleton, TypeMatch typeMatch) {
		this.singleton = singleton;
		this.typeMatch = typeMatch;
	}

	@Override
	public Construction<T> perform(Registry registry) {
		return RegisteredSingleton.create(singleton, typeMatch);
	}

	@Override
	public Registration<T> with(Annotation annotation) {
		TypeMatch newTypeMatch = typeMatch.withAnnotation(annotation);
		return new SingletonRegistration<>(singleton, newTypeMatch);
	}
}
