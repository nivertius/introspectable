package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

import com.google.common.collect.ImmutableSet;

final class TypeRegistration<T> implements Registration<T> {
	private final Class<T> type;
	private final ImmutableSet<Annotation> additionalQualifiers;

	public static <T> TypeRegistration<T> create(Class<T> type) {
		return new TypeRegistration<>(type, ImmutableSet.of());
	}

	private TypeRegistration(Class<T> type, ImmutableSet<Annotation> additionalQualifiers) {
		this.type = type;
		this.additionalQualifiers = additionalQualifiers;
	}

	@Override
	public Construction<T> perform(Registry registry) {
		return PreparedConstruction.create(type, RegistryProvider.of(type, registry), additionalQualifiers);
	}

	@Override
	public Registration<T> with(Annotation qualifier) {
		ImmutableSet<Annotation> newAdditionalQualifiers = ImmutableSet.<Annotation>builder()
			.addAll(additionalQualifiers).add(qualifier).build();
		return new TypeRegistration<>(type, newAdditionalQualifiers);
	}
}
