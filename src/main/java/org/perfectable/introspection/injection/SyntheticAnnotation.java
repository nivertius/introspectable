package org.perfectable.introspection.injection;

import java.lang.annotation.Annotation;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

final class SyntheticAnnotation implements Annotation {
	private final Class<? extends Annotation> type;

	static SyntheticAnnotation ofType(Class<? extends Annotation> annotationType) {
		requireNonNull(annotationType);
		checkArgument(annotationType.getDeclaredMethods().length == 0,
			"Annotation type %s cannot be synthesized, because it declares methods", annotationType);
		return new SyntheticAnnotation(annotationType);
	}

	private SyntheticAnnotation(Class<? extends Annotation> type) {
		this.type = type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Annotation)) {
			return false;
		}
		Annotation other = (Annotation) obj;
		return type.equals(other.annotationType());
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public String toString() {
		return "@" + type.getName();
	}

	@Override
	public Class<? extends Annotation> annotationType() {
		return type;
	}


}
