package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableSet;

import static org.perfectable.introspection.Introspections.introspect;

abstract class PreparedConstruction<T> implements Construction<T> {
	public static <T> PreparedConstruction<T> create(Class<T> createdClass,
													 Provider<T> provider,
													 ImmutableSet<Annotation> additionalQualifiers) {
		ImmutableSet<Annotation> qualifiers = Construction.mergeQualifiers(createdClass, additionalQualifiers);
		boolean isSingleton = introspect(createdClass).annotations().typed(Singleton.class).isPresent();
		return isSingleton ? new SingletonConstruction<>(createdClass, provider, qualifiers)
			: new PrototypeConstruction<>(createdClass, provider, qualifiers);
	}

	private final Class<T> createdClass;
	private final Provider<T> provider;
	private final ImmutableSet<Annotation> qualifiers;

	private PreparedConstruction(Class<T> createdClass, Provider<T> provider, ImmutableSet<Annotation> qualifiers) {
		this.createdClass = createdClass;
		this.provider = provider;
		this.qualifiers = qualifiers;
	}

	protected final T provide() {
		return provider.get();
	}

	@Override
	public boolean matches(Query<?> query) {
		return query.matches(createdClass, qualifiers);
	}

	private static class PrototypeConstruction<T> extends PreparedConstruction<T> {
		PrototypeConstruction(Class<T> createdClass, Provider<T> provider, ImmutableSet<Annotation> qualifiers) {
			super(createdClass, provider, qualifiers);
		}

		@Override
		public T construct() {
			return provide();
		}
	}

	private static class SingletonConstruction<T> extends PreparedConstruction<T> {
		@Nullable
		private T singletonInstance;

		SingletonConstruction(Class<T> createdClass, Provider<T> provider, ImmutableSet<Annotation> qualifiers) {
			super(createdClass, provider, qualifiers);
		}

		@Override
		public T construct() {
			if (singletonInstance == null) {
				singletonInstance = provide();
			}
			return singletonInstance;
		}
	}
}
