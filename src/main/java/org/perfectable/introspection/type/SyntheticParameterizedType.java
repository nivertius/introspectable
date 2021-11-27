package org.perfectable.introspection.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

final class SyntheticParameterizedType implements ParameterizedType, AnnotatedParameterizedType {
	private final Class<?> rawType;
	private final @Nullable SyntheticAnnotatedType ownerType;
	private final AnnotationContainer annotations;
	private final SyntheticAnnotatedType[] typeArguments;

	SyntheticParameterizedType(Class<?> rawType, @Nullable SyntheticAnnotatedType ownerType,
							   AnnotationContainer annotations,
							   SyntheticAnnotatedType[] typeArguments) {
		checkArgument(typeArguments.length == rawType.getTypeParameters().length);
		this.rawType = rawType;
		this.ownerType = ownerType;
		this.annotations = annotations;
		this.typeArguments = typeArguments.clone();
	}

	@Override
	public Type[] getActualTypeArguments() {
		return SyntheticAnnotatedType.typeCopy(typeArguments);
	}

	@Override
	public Class<?> getRawType() {
		return rawType;
	}

	@SuppressWarnings("override.return.invalid")
	@Override
	public @Nullable Type getOwnerType() {
		if (ownerType == null) {
			return null;
		}
		return ownerType.getType();
	}

	@Override
	public String getTypeName() {
		String argumentNames = Stream.of(typeArguments)
			.map(SyntheticAnnotatedType::getTypeName)
			.collect(Collectors.joining(",", "<", ">"));
		return rawType.getTypeName() + argumentNames;
	}

	@Override
	public String toString() {
		return getTypeName();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof ParameterizedType)) {
			return false;
		}
		ParameterizedType other = (ParameterizedType) obj;
		return rawType.equals(other.getRawType())
			&& Objects.equals(ownerType, other.getOwnerType())
			&& Arrays.equals(typeArguments, other.getActualTypeArguments());
	}

	@Override
	public int hashCode() {
		return Objects.hash(rawType, ownerType, Arrays.hashCode(typeArguments));
	}

	@Override
	public AnnotatedType[] getAnnotatedActualTypeArguments() {
		return typeArguments.clone();
	}

	public @Nullable AnnotatedType getAnnotatedOwnerType() {
		return ownerType;
	}

	@Override
	public Type getType() {
		return this;
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
