package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkNotNull;

class RegisteredSingleton<T> implements Construction<T> {
	private final T singleton;
	private final CompositeTypeMatch typeMatch;

	public static <T> RegisteredSingleton<T> create(T singleton, Annotation... qualifiers) {
		checkNotNull(singleton);
		CompositeTypeMatch typeMatch = CompositeTypeMatch.create(singleton.getClass(), qualifiers);
		return new RegisteredSingleton<>(singleton, typeMatch);
	}

	RegisteredSingleton(T singleton, CompositeTypeMatch typeMatch) {
		this.singleton = singleton;
		this.typeMatch = typeMatch;
	}

	@Override
	public T create(Class<T> targetClass) {
		return targetClass.cast(singleton);
	}

	@Override
	public boolean matches(Class<?> type, Annotation... qualifiers) {
		return typeMatch.matches(type, qualifiers);
	}
}
