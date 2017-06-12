package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import static java.util.Objects.requireNonNull;

final class RegisteredSingleton<T> implements Construction<T> {
	private final T singleton;
	private final Set<Annotation> qualifiers;

	public static <T> RegisteredSingleton<T> create(T singleton, Set<Annotation> additionalQualifiers) {
		requireNonNull(singleton);
		ImmutableSet<Annotation> qualifiers =
			Construction.mergeQualifiers(singleton.getClass(), additionalQualifiers);
		return new RegisteredSingleton<>(singleton, qualifiers);
	}

	private RegisteredSingleton(T singleton, Set<Annotation> qualifiers) {
		this.singleton = singleton;
		this.qualifiers = qualifiers;
	}

	@Override
	public T construct() {
		return singleton;
	}

	@Override
	public boolean matches(Query<?> query) {
		return query.matches(singleton.getClass(), qualifiers);
	}
}
