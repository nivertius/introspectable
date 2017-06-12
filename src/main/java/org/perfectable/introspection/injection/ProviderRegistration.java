package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import javax.inject.Provider;

final class ProviderRegistration<T> implements Registration<T> {
	private final Class<T> type;
	private final Provider<T> provider;
	private final TypeMatch typeMatch;

	public static <T> ProviderRegistration<T> create(Class<T> type, Provider<T> provider) {
		TypeMatch typeMatch = TypeMatch.create(type);
		return new ProviderRegistration<>(type, provider, typeMatch);
	}

	private ProviderRegistration(Class<T> type, Provider<T> provider, TypeMatch typeMatch) {
		this.type = type;
		this.provider = provider;
		this.typeMatch = typeMatch;
	}

	@Override
	public Construction<T> perform(Registry registry) {
		return PreparedConstruction.create(type, provider, typeMatch);
	}

	@Override
	public Registration<T> with(Annotation annotation) {
		TypeMatch newTypeMatch = typeMatch.withAnnotation(annotation);
		return new ProviderRegistration<>(type, provider, newTypeMatch);
	}
}
