package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Provider;

public class StandardRegistry implements Registry, Configuration {
	private final Set<Construction<?>> preparedConstructions = new HashSet<>();

	public static StandardRegistry create() {
		return new StandardRegistry();
	}

	@Override
	public final <T> Registrator<T> register(T singleton, Annotation... qualifiers) {
		RegisteredSingleton<T> registeredSingleton = RegisteredSingleton.create(singleton, qualifiers);
		preparedConstructions.add(registeredSingleton);
		return registeredSingleton;
	}

	@Override
	public final <T> Registrator<T> register(Class<T> createdClass, Annotation... qualifiers) {
		Provider<T> provider = RegistryProvider.of(createdClass, this);
		return register(createdClass, provider);
	}

	@Override
	public <T> Registrator<T> register(Class<T> createdClass, Provider<T> provider, Annotation... qualifiers) {
		PreparedConstruction<T> injection = PreparedConstruction.create(createdClass, provider, qualifiers);
		preparedConstructions.add(injection);
		return injection;
	}

	@Override
	public <T> T fetch(Query<T> query) {
		for (Construction<?> construction : preparedConstructions) {
			if (construction.matches(query)) {
				@SuppressWarnings("unchecked")
				T casted = ((Construction<T>) construction).construct();
				return casted;
			}
		}
		throw new IllegalArgumentException("No construction matches " + query);
	}
}
