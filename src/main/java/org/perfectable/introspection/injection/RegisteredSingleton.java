package org.perfectable.introspection.injection;

import static java.util.Objects.requireNonNull;

final class RegisteredSingleton<T> implements Construction<T> {
	private final T singleton;
	private final TypeMatch typeMatch;

	public static <T> RegisteredSingleton<T> create(T singleton, TypeMatch typeMatch) {
		requireNonNull(singleton);
		return new RegisteredSingleton<>(singleton, typeMatch);
	}

	private RegisteredSingleton(T singleton, TypeMatch typeMatch) {
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
}
