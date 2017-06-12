package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.inject.Singleton;

import static org.perfectable.introspection.Introspections.introspect;

abstract class PreparedConstruction<T> implements Construction<T>, Configuration.Registrator<T> {
	public static <T> PreparedConstruction<T> create(Class<T> createdClass,
													 Provider<T> provider, Annotation... qualifiers) {
		TypeMatch typeMatch = SimpleTypeMatch.create(createdClass, qualifiers);
		boolean isSingleton = introspect(createdClass).annotations().typed(Singleton.class).isPresent();
		return isSingleton ? new SingletonConstruction<>(provider, typeMatch)
				: new PrototypeConstruction<>(provider, typeMatch);
	}

	private TypeMatch typeMatch;
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

	@Override
	public void as(Class<? super T> injectableClass, Annotation... qualifiers) {
		typeMatch = typeMatch.orElse(SimpleTypeMatch.create(injectableClass, qualifiers));
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
