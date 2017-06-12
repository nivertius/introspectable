package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

final class TypeRegistration<T> implements Registration<T> {
	private final Class<T> type;
	private final TypeMatch typeMatch;

	public static <T> TypeRegistration<T> create(Class<T> type) {
		TypeMatch typeMatch = TypeMatch.create(type);
		return new TypeRegistration<>(type, typeMatch);
	}

	private TypeRegistration(Class<T> type, TypeMatch typeMatch) {
		this.type = type;
		this.typeMatch = typeMatch;
	}

	@Override
	public Construction<T> perform(Registry registry) {
		return PreparedConstruction.create(type, RegistryProvider.of(type, registry), typeMatch);
	}

	@Override
	public Registration<T> with(Annotation annotation) {
		TypeMatch newTypeMatch = typeMatch.withAnnotation(annotation);
		return new TypeRegistration<>(type, newTypeMatch);
	}
}
