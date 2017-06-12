package org.perfectable.introspection.injection;

import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.inject.Singleton;

import static org.perfectable.introspection.Introspections.introspect;

abstract class PreparedConstruction<T> implements Construction<T> {
	public static <T> PreparedConstruction<T> create(Class<T> createdClass,
													 Provider<T> provider, TypeMatch typeMatch) {
		boolean isSingleton = introspect(createdClass).annotations().typed(Singleton.class).isPresent();
		return isSingleton ? new SingletonConstruction<>(provider, typeMatch)
				: new PrototypeConstruction<>(provider, typeMatch);
	}

	private final TypeMatch typeMatch;
	private final Provider<T> provider;

	private PreparedConstruction(Provider<T> provider, TypeMatch typeMatch) {
		this.provider = provider;
		this.typeMatch = typeMatch;
	}

	protected final T provide() {
		return provider.get();
	}

	@Override
	public boolean matches(Query<?> query) {
		return typeMatch.matches(query);
	}

	private static class PrototypeConstruction<T> extends PreparedConstruction<T> {
		PrototypeConstruction(Provider<T> provider, TypeMatch typeMatch) {
			super(provider, typeMatch);
		}

		@Override
		public T construct() {
			return provide();
		}
	}

	private static class SingletonConstruction<T> extends PreparedConstruction<T> {
		@Nullable
		private T singletonInstance;

		SingletonConstruction(Provider<T> provider, TypeMatch typeMatch) {
			super(provider, typeMatch);
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
