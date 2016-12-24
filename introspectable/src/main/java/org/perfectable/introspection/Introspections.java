package org.perfectable.introspection;

public final class Introspections {

	public static <X> ClassIntrospection<X> introspect(Class<X> type) {
		return ClassIntrospection.of(type);
	}

	private Introspections() {
		// utility
	}
}
