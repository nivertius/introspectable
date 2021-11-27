package org.perfectable.introspection.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

final class SyntheticGenericArrayType implements GenericArrayType, AnnotatedArrayType {
	private final AnnotationContainer componentAnnotations;
	private final SyntheticAnnotatedType componentType;

	SyntheticGenericArrayType(SyntheticAnnotatedType componentType, AnnotationContainer componentAnnotations) {
		this.componentType = componentType;
		this.componentAnnotations = componentAnnotations;
	}

	@Override
	public Type getGenericComponentType() {
		return componentType.getType();
	}

	@Override
	public String getTypeName() {
		return componentType.getTypeName() + "[]";
	}

	@SuppressWarnings("ChainOfInstanceofChecks")
	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof AnnotatedArrayType) {
			AnnotatedType thatComponent = ((AnnotatedArrayType) obj).getAnnotatedGenericComponentType();
			if (!componentAnnotations.sameAnnotationsOn(thatComponent)) {
				return false;
			}
			return Objects.equals(componentType, thatComponent);
		}
		if (obj instanceof GenericArrayType) {
			if (!componentAnnotations.isEmpty()) {
				return false;
			}
			Type thatComponent = ((GenericArrayType) obj).getGenericComponentType();
			return Objects.equals(componentType, thatComponent);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(componentType, componentAnnotations);
	}

	@Override
	public String toString() {
		return getTypeName();
	}

	@Override
	public AnnotatedType getAnnotatedGenericComponentType() {
		return componentType;
	}

	public @Nullable AnnotatedType getAnnotatedOwnerType() {
		return null;
	}

	@Override
	public Type getType() {
		return this;
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends @Nullable Annotation> annotationClass) {
		return componentAnnotations.isAnnotationPresent(annotationClass);
	}

	@Override
	public <T extends @Nullable Annotation> @Nullable T getAnnotation(Class<T> annotationClass) {
		return componentAnnotations.getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return componentAnnotations.getAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return componentAnnotations.getDeclaredAnnotations();
	}

	@Override
	public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
		return componentAnnotations.getAnnotationsByType(annotationClass);
	}

	@Override
	public <T extends Annotation> @Nullable T getDeclaredAnnotation(Class<T> annotationClass) {
		return componentAnnotations.getDeclaredAnnotation(annotationClass);
	}

	@Override
	public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
		return componentAnnotations.getDeclaredAnnotationsByType(annotationClass);
	}
}
