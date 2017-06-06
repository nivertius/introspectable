package org.perfectable.introspection.injection;

@FunctionalInterface
interface TypeMatch {

	boolean matches(Query<?> query);

}
