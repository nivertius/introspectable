package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.perfectable.introspection.Introspections.introspect;

class RegisteredSingleton<T> {
	private final T singleton;

	public static <T> RegisteredSingleton<T> create(T singleton) {
		checkNotNull(singleton);
		return new RegisteredSingleton<>(singleton);
	}

	RegisteredSingleton(T singleton) {
		this.singleton = singleton;
	}

	public T asInjectable() {
		return singleton;
	}

	public boolean matches(Class<?> type, Annotation... qualifiers) {
		if (!type.isInstance(singleton)) {
			return false;
		}
		ImmutableList<Annotation> annotationsList = ImmutableList.copyOf(qualifiers);
		if (!introspect(singleton.getClass()).annotations() // SUPPRESSS SimplifyBooleanReturns
				.stream().allMatch(annotationsList::contains)) {
			return false;
		}
		return true;
	}
}
