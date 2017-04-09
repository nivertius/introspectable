package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import javax.inject.Provider;

public interface Configuration {

	<T> Registrator<T> register(T singleton, Annotation... qualifiers);

	<T> Registrator<T> register(Class<T> createdClass, Annotation... qualifiers);

	<T> Registrator<T> register(Class<T> createdClass, Provider<T> provider, Annotation... qualifiers);

	interface Registrator<T> {

		void as(Class<? super T> injectableClass, Annotation... qualifiers);
	}

}
