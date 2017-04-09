package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import javax.annotation.Nullable;
import javax.inject.Provider;
import javax.inject.Singleton;

import static org.perfectable.introspection.Introspections.introspect;

abstract class PreparedConstruction<T> implements Construction<T> {
	public static <T> PreparedConstruction<T> create(Class<T> createdClass,
													 Provider<T> provider, Annotation... qualifiers) {
		CompositeTypeMatch typeMatch = CompositeTypeMatch.create(createdClass, qualifiers);
		if (introspect(createdClass).annotations().typed(Singleton.class).isPresent()) {
			return new SingletonConstruction<T>(provider, typeMatch);
		}
		else {
			return new PrototypeConstruction<T>(provider, typeMatch);
		}
	}

	private final CompositeTypeMatch typeMatch;
	private final Provider<T> provider;

	private PreparedConstruction(Provider<T> provider, CompositeTypeMatch typeMatch) {
		this.provider = provider;
		this.typeMatch = typeMatch;
	}

	protected final T construct(Class<T> targetClass) {
		return targetClass.cast(provider.get());

	}

	@Override
	public boolean matches(Class<?> type, Annotation... qualifiers) {
		return typeMatch.matches(type, qualifiers);
	}

	private static class PrototypeConstruction<T> extends PreparedConstruction<T> {
		PrototypeConstruction(Provider<T> provider, CompositeTypeMatch typeMatch) {
			super(provider, typeMatch);
		}

		@Override
		public T create(Class<T> targetClass) {
			return construct(targetClass);
		}
	}

	private static class SingletonConstruction<T> extends PreparedConstruction<T> {
		@Nullable
		private T singletonInstance;

		SingletonConstruction(Provider<T> provider, CompositeTypeMatch typeMatch) {
			super(provider, typeMatch);
		}

		@Override
		public T create(Class<T> targetClass) {
			if (singletonInstance == null) {
				singletonInstance = construct(targetClass);
			}
			return singletonInstance;
		}
	}
}
