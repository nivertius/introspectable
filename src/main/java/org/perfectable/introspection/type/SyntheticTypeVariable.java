package org.perfectable.introspection.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

final class SyntheticTypeVariable<D extends GenericDeclaration> implements TypeVariable<D>, AnnotatedTypeVariable {
	private final String name;
	private final D declaration;
	private final AnnotationContainer annotations;
	private final SyntheticAnnotatedType[] bounds;

	SyntheticTypeVariable(String name, D declaration, AnnotationContainer annotations,
						  SyntheticAnnotatedType[] bounds) {
		this.name = name;
		this.declaration = declaration;
		this.annotations = annotations;
		this.bounds = bounds;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public D getGenericDeclaration() {
		return declaration;
	}

	@Override
	public Type[] getBounds() {
		return SyntheticAnnotatedType.typeCopy(bounds);
	}

	@Override
	public String getTypeName() {
		return getName();
	}

	@Override
	public AnnotatedType[] getAnnotatedBounds() {
		return bounds.clone();
	}

	public @Nullable AnnotatedType getAnnotatedOwnerType() {
		return null;
	}

	@Override
	public Type getType() {
		return this;
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof TypeVariable<?>)) {
			return false;
		}
		TypeVariable<?> other = (TypeVariable<?>) obj;
		return Objects.equals(name, other.getName())
			&& Objects.equals(declaration, other.getGenericDeclaration())
			&& Arrays.equals(bounds, other.getBounds());
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, declaration, Arrays.hashCode(bounds));
	}

	@Override
	public String toString() {
		return getTypeName();
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends @Nullable Annotation> annotationClass) {
		return annotations.isAnnotationPresent(annotationClass);
	}

	@Override
	public <T extends @Nullable Annotation> @Nullable T getAnnotation(Class<T> annotationClass) {
		return annotations.getAnnotation(annotationClass);
	}

	@Override
	public Annotation[] getAnnotations() {
		return annotations.getAnnotations();
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return annotations.getDeclaredAnnotations();
	}

	@Override
	public <T extends Annotation> T[] getAnnotationsByType(Class<T> annotationClass) {
		return annotations.getAnnotationsByType(annotationClass);
	}

	@Override
	public <T extends Annotation> @Nullable T getDeclaredAnnotation(Class<T> annotationClass) {
		return annotations.getDeclaredAnnotation(annotationClass);
	}

	@Override
	public <T extends Annotation> T[] getDeclaredAnnotationsByType(Class<T> annotationClass) {
		return annotations.getDeclaredAnnotationsByType(annotationClass);
	}
}
