package org.perfectable.introspection.injection;

@FunctionalInterface
interface TypeMatch {

	boolean matches(Query<?> query);

	default TypeMatch orElse(TypeMatch other) {
		return CompositeTypeMatch.create(this, other);
	}
}
