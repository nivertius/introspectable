package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import javax.inject.Provider;

public interface Registration<T> {
	static <T> Registration<T> singleton(T singleton) {
		return SingletonRegistration.create(singleton);
	}

	static <T> Registration<T> type(Class<T> type) {
		return TypeRegistration.create(type);
	}

	static <T> Registration<T> provider(Class<T> type, Provider<T> provider) {
		return ProviderRegistration.create(type, provider);
	}

	Registration<T> with(Annotation annotation);

	default Registration<T> with(Class<? extends Annotation> synthesizedAnnotation) {
		return with(SyntheticAnnotation.ofType(synthesizedAnnotation));
	}

	Construction<T> perform(Registry registry);
}
