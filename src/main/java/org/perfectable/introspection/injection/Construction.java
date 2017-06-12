package org.perfectable.introspection.injection;

public interface Construction<T> {
	T construct();

	boolean matches(Query<?> query);
}
