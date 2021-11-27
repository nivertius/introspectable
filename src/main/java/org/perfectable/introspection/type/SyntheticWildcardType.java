package org.perfectable.introspection.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

final class SyntheticWildcardType implements WildcardType, AnnotatedWildcardType {

	private static final Collector<CharSequence, ?, String> BOUND_JOINER = Collectors.joining(" & ");

	private final AnnotationContainer annotations;
	private final SyntheticAnnotatedType[] lowerBounds;
	private final SyntheticAnnotatedType[] upperBounds;

	SyntheticWildcardType(AnnotationContainer annotations, SyntheticAnnotatedType[] lowerBounds,
						  SyntheticAnnotatedType[] upperBounds) {
		this.annotations = annotations;
		this.lowerBounds = lowerBounds.clone();
		this.upperBounds = upperBounds.clone();
	}

	@Override
	public Type[] getLowerBounds() {
		return SyntheticAnnotatedType.typeCopy(lowerBounds);
	}

	@Override
	public Type[] getUpperBounds() {
		return SyntheticAnnotatedType.typeCopy(upperBounds);
	}

	@Override
	public String getTypeName() {
		StringBuilder builder = new StringBuilder("?");
		if (lowerBounds.length > 0) {
			String lowerBoundsString =
				Stream.of(lowerBounds).map(SyntheticAnnotatedType::getTypeName).collect(BOUND_JOINER);
			builder.append(" super ").append(lowerBoundsString);
		}
		if (upperBounds.length > 0) {
			String upperBoundsString =
				Stream.of(upperBounds).map(SyntheticAnnotatedType::getTypeName).collect(BOUND_JOINER);
			builder.append(" extends ").append(upperBoundsString);
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return getTypeName();
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (!(obj instanceof WildcardType)) {
			return false;
		}
		WildcardType other = (WildcardType) obj;
		return Arrays.equals(lowerBounds, other.getLowerBounds())
			&& Arrays.equals(upperBounds, other.getUpperBounds());
	}

	@Override
	public int hashCode() {
		return Objects.hash(Arrays.hashCode(lowerBounds), Arrays.hashCode(upperBounds));
	}

	@Override
	public AnnotatedType[] getAnnotatedLowerBounds() {
		return lowerBounds.clone();
	}

	@Override
	public AnnotatedType[] getAnnotatedUpperBounds() {
		return upperBounds.clone();
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
