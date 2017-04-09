package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import java.util.Arrays;
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
	public <T> T fetch(Class<T> targetClass, Annotation... qualifiers) {
		for (Construction<?> construction : preparedConstructions) {
			if (construction.matches(targetClass, qualifiers)) {
				@SuppressWarnings("unchecked")
				T casted = ((Construction<T>) construction).create(targetClass);
				return casted;
			}
		}
		throw new IllegalArgumentException("Don't know how to create " + targetClass
				+ " with qualifiers " + Arrays.toString(qualifiers));
	}
}
