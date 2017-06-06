package org.perfectable.introspection.injection;

interface Construction<T> {
	T construct();

	boolean matches(Query<?> query);
}
