package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.inject.Qualifier;

import com.google.common.collect.ImmutableSet;

import static org.perfectable.introspection.Introspections.introspect;

final class TypeMatch {
	private final Class<?> targetClass;
	private final Set<Annotation> annotations;

	private TypeMatch(Class<?> targetClass, Set<Annotation> annotations) {
		this.targetClass = targetClass;
		this.annotations = annotations;
	}

	public static TypeMatch create(Class<?> targetClass, Annotation... qualifiers) {
		Set<Annotation> annotations =
				introspect(targetClass).annotations().annotatedWith(Qualifier.class).stream()
						.collect(Collectors.toCollection(HashSet::new));
		annotations.addAll(Arrays.asList(qualifiers));
		return new TypeMatch(targetClass, annotations);
	}

	boolean matches(Query<?> query) {
		return query.matches(targetClass, annotations);
	}

	TypeMatch withAnnotation(Annotation annotation) {
		Set<Annotation> newAnnotations = ImmutableSet.<Annotation>builder()
			.addAll(annotations).add(annotation).build();
		return new TypeMatch(targetClass, newAnnotations);
	}
}
