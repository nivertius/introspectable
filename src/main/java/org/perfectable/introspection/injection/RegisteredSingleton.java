package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

import static java.util.Objects.requireNonNull;

final class RegisteredSingleton<T> implements Construction<T>, Configuration.Registrator<T> {
	private final T singleton;
	private final CompositeTypeMatch typeMatch;

	public static <T> RegisteredSingleton<T> create(T singleton, Annotation... qualifiers) {
		requireNonNull(singleton);
		CompositeTypeMatch typeMatch = CompositeTypeMatch.create(singleton.getClass(), qualifiers);
		return new RegisteredSingleton<>(singleton, typeMatch);
	}

	private RegisteredSingleton(T singleton, CompositeTypeMatch typeMatch) {
		this.singleton = singleton;
		this.typeMatch = typeMatch;
	}

	@Override
	public T construct() {
		return singleton;
	}

	@Override
	public boolean matches(Query<?> query) {
		return typeMatch.matches(query);
	}

	@Override
	public void as(Class<? super T> injectableClass, Annotation... qualifiers) {
		typeMatch.add(injectableClass, qualifiers);
	}
}
