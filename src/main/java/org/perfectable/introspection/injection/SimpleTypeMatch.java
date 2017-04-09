package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Qualifier;

import com.google.common.collect.Sets;

import static org.perfectable.introspection.Introspections.introspect;

class SimpleTypeMatch<T> implements TypeMatch {
	private final Class<T> targetClass;
	private final Set<Annotation> annotations;

	SimpleTypeMatch(Class<T> targetClass, Set<Annotation> annotations) {
		this.targetClass = targetClass;
		this.annotations = annotations;
	}

	public static <T> SimpleTypeMatch<T> create(Class<T> targetClass, Annotation... qualifiers) {
		Set<Annotation> annotations =
				introspect(targetClass).annotations().annotatedWith(Qualifier.class).stream()
						.collect(Collectors.toCollection(HashSet::new));
		annotations.addAll(Arrays.asList(qualifiers));
		return new SimpleTypeMatch<T>(targetClass, annotations);
	}

	@Override
	public boolean matches(Class<?> type, Annotation... qualifiers) {
		return type.isAssignableFrom(targetClass)
				&& Sets.newHashSet(qualifiers).equals(annotations);
	}
}
