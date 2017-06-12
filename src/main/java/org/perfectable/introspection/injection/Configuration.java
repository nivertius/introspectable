package org.perfectable.introspection.injection;

import javax.inject.Provider;

public interface Configuration {

	<T> void register(Registration<T> construction);

	default <T> void registerSingleton(T singleton) {
		register(Registration.singleton(singleton));
	}

	default <T> void registerType(Class<T> type) {
		register(Registration.type(type));
	}

	default <T> void registerProvider(Class<T> type, Provider<T> provider) {
		register(Registration.provider(type, provider));
	}


}
