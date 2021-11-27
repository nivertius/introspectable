package org.perfectable.introspection.type;

import org.perfectable.introspection.query.AnnotationQuery;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.Nullable;

final class AnnotationContainer {
	public static final AnnotationContainer EMPTY = new AnnotationContainer(new Annotation[0]);

	private final Annotation[] annotations;

	private AnnotationContainer(Annotation[] annotations) {
		this.annotations = annotations.clone();
	}

	public static AnnotationContainer extract(Type type) {
		if (!(type instanceof AnnotatedElement)) {
			return EMPTY;
		}
		return extract((AnnotatedElement) type);
	}

	public static AnnotationContainer extract(AnnotatedElement element) {
		return create(element.getAnnotations());
	}

	public static AnnotationContainer create(Set<Annotation> annotations) {
		return new AnnotationContainer(annotations.toArray(new Annotation[0]));
	}

	public static AnnotationContainer create(Annotation... annotations) {
		return new AnnotationContainer(annotations);
	}

	public boolean isAnnotationPresent(Class<? extends @Nullable Annotation> annotationClass) {
		return getAnnotation(annotationClass) != null;
	}

	public <T extends Annotation> @Nullable T getAnnotation(Class<T> annotationClass) {
		return AnnotationQuery.fromElements(annotations)
			.typed(annotationClass)
			.option().orElse(null);
	}

	public Annotation[] getDeclaredAnnotations() {
		return annotations.clone();
	}

	public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
		return AnnotationQuery.fromElements(annotations)
			.withRepeatableUnroll()
			.typed(annotationClass)
			.stream()
			.toArray(length -> safeCreateArray(annotationClass, length));
	}

	public Annotation[] getAnnotations() {
		return getDeclaredAnnotations();
	}


	public <T extends Annotation> @Nullable T getDeclaredAnnotation(Class<T> annotationClass) {
		return getAnnotation(annotationClass);
	}

	public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
		return getAnnotationsByType(annotationClass);
	}

	public boolean isEmpty() {
		return annotations.length == 0;
	}

	public boolean sameAnnotationsOn(AnnotatedType other) {
		Annotation[] otherAnnotations = other.getAnnotations();
		return ImmutableSet.copyOf(annotations).equals(ImmutableSet.copyOf(otherAnnotations));
	}

	@SuppressWarnings("unchecked")
	private static <T extends Annotation> T[] safeCreateArray(Class<T> annotationClass, int length) {
		return (T[]) Array.newInstance(annotationClass, length);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof AnnotationContainer)) {
			return false;
		}
		AnnotationContainer that = (AnnotationContainer) obj;
		return Arrays.equals(annotations, that.annotations);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(annotations);
	}

}
