package org.perfectable.introspection.injection;

import java.util.HashSet;
import java.util.Set;

public final class StandardRegistry implements Registry, Configuration {
	private final Set<Construction<?>> preparedConstructions = new HashSet<>();

	public static StandardRegistry create() {
		return new StandardRegistry();
	}

	private StandardRegistry() {
		// final
	}

	@Override
	public <T> void register(Registration<T> registration) {
		preparedConstructions.add(registration.perform(this));
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
