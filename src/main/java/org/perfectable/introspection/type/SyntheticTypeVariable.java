package org.perfectable.introspection.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nullable;

final class SyntheticTypeVariable<D extends GenericDeclaration> implements TypeVariable<D> {
	private final String name;
	private final D declaration;
	private final Type[] bounds;

	SyntheticTypeVariable(String name, D declaration, Type[] bounds) { // SUPPRESS UseVarargs
		this.name = name;
		this.declaration = declaration;
		this.bounds = bounds.clone();
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
		return bounds.clone();
	}

	@Override
	public String getTypeName() {
		return getName();
	}

	@Override
	public AnnotatedType[] getAnnotatedBounds() {
		return new AnnotatedType[0];
	}

	@Override
	@Nullable
	public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
		return null;
	}

	@Override
	public Annotation[] getAnnotations() {
		return new Annotation[0];
	}

	@Override
	public Annotation[] getDeclaredAnnotations() {
		return new Annotation[0];
	}

	@Override
	public boolean equals(Object obj) {
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
}
