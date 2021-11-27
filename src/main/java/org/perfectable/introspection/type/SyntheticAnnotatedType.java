package org.perfectable.introspection.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;
import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

final class SyntheticAnnotatedType implements AnnotatedType, Type {
	private final Type type;
	private final AnnotationContainer annotations;

	private SyntheticAnnotatedType(Type type, AnnotationContainer annotations) {
		this.type = type;
		this.annotations = annotations;
	}

	public static Type[] typeCopy(SyntheticAnnotatedType[] types) {
		Type[] result = new Type[types.length];
		for (int i = 0; i < types.length; i++) {
			result[i] = types[i].getType();
		}
		return result;
	}

	public static SyntheticAnnotatedType wrap(Type type) {
		AnnotationContainer annotations;
		if (type instanceof AnnotatedType) {
			AnnotatedType annotatedType = (AnnotatedType) type;
			annotations = AnnotationContainer.create(annotatedType.getAnnotations());
		}
		else {
			annotations = AnnotationContainer.EMPTY;
		}
		return new SyntheticAnnotatedType(type, annotations);
	}

	public static SyntheticAnnotatedType[] wrap(Type[] type) {
		SyntheticAnnotatedType[] result = new SyntheticAnnotatedType[type.length];
		for (int i = 0; i < type.length; i++) {
			result[i] = wrap(type[i]);
		}
		return result;
	}

	public static SyntheticAnnotatedType create(Type type, Annotation... annotations) {
		return new SyntheticAnnotatedType(type, AnnotationContainer.create(annotations));
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public String getTypeName() {
		return type.getTypeName();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof SyntheticAnnotatedType)) {
			return false;
		}
		SyntheticAnnotatedType that = (SyntheticAnnotatedType) obj;
		return type.equals(that.type)
			&& annotations.equals(that.annotations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(type, annotations);
	}

	@Override
	public boolean isAnnotationPresent(Class<? extends @Nullable Annotation> annotationClass) {
		return annotations.isAnnotationPresent(annotationClass);
	}

	@Override
	public <T extends Annotation> @Nullable T getAnnotation(Class<T> annotationClass) {
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
