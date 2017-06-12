package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import java.util.Set;
import javax.inject.Qualifier;

import com.google.common.collect.ImmutableSet;

import static org.perfectable.introspection.Introspections.introspect;

interface Construction<T> {
	T construct();

	boolean matches(Query<?> query);

	static ImmutableSet<Annotation> mergeQualifiers(Class<?> sourceClass, Set<Annotation> additionalQualifiers) {
		ImmutableSet.Builder<Annotation> qualifiersBuilder = ImmutableSet.<Annotation>builder()
			.addAll(additionalQualifiers);
		introspect(sourceClass)
			.annotations().annotatedWith(Qualifier.class).stream()
			.forEach(qualifiersBuilder::add);
		return qualifiersBuilder.build();
	}
}
