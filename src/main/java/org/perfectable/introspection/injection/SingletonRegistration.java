package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

import com.google.common.collect.ImmutableSet;

final class SingletonRegistration<T> implements Registration<T> {
	private final T singleton;
	private final ImmutableSet<Annotation> additionalQualifiers;

	public static <T> SingletonRegistration<T> create(T singleton) {
		return new SingletonRegistration<>(singleton, ImmutableSet.of());
	}

	private SingletonRegistration(T singleton, ImmutableSet<Annotation> additionalQualifiers) {
		this.singleton = singleton;
		this.additionalQualifiers = additionalQualifiers;
	}

	@Override
	public Construction<T> perform(Registry registry) {
		return RegisteredSingleton.create(singleton, additionalQualifiers);
	}

	@Override
	public Registration<T> with(Annotation qualifier) {
		ImmutableSet<Annotation> newAdditionalQualifiers = ImmutableSet.<Annotation>builder()
			.addAll(additionalQualifiers).add(qualifier).build();
		return new SingletonRegistration<>(singleton, newAdditionalQualifiers);
	}
}
