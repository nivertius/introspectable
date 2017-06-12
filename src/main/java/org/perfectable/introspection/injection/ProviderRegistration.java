package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import javax.inject.Provider;

import com.google.common.collect.ImmutableSet;

final class ProviderRegistration<T> implements Registration<T> {
	private final Class<T> type;
	private final Provider<T> provider;
	private final ImmutableSet<Annotation> additionalQualifiers;

	public static <T> ProviderRegistration<T> create(Class<T> type, Provider<T> provider) {
		return new ProviderRegistration<>(type, provider, ImmutableSet.of());
	}

	private ProviderRegistration(Class<T> type, Provider<T> provider, ImmutableSet<Annotation> additionalQualifiers) {
		this.type = type;
		this.provider = provider;
		this.additionalQualifiers = additionalQualifiers;
	}

	@Override
	public Construction<T> perform(Registry registry) {
		return PreparedConstruction.create(type, provider, additionalQualifiers);
	}

	@Override
	public Registration<T> with(Annotation qualifier) {
		ImmutableSet<Annotation> newAdditionalQualifiers = ImmutableSet.<Annotation>builder()
			.addAll(additionalQualifiers).add(qualifier).build();
		return new ProviderRegistration<>(type, provider, newAdditionalQualifiers);
	}
}
