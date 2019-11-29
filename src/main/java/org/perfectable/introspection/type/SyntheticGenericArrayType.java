package org.perfectable.introspection.type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.Objects;

final class SyntheticGenericArrayType implements GenericArrayType {
	private final Type componentType;

	SyntheticGenericArrayType(Type componentType) {
		this.componentType = componentType;
	}

	@Override
	public Type getGenericComponentType() {
		return componentType;
	}

	@Override
	public String getTypeName() {
		return componentType.getTypeName() + "[]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof GenericArrayType)) {
			return false;
		}
		GenericArrayType that = (GenericArrayType) obj;
		return Objects.equals(componentType, that.getGenericComponentType());
	}

	@Override
	public int hashCode() {
		return Objects.hash(componentType);
	}

	@Override
	public String toString() {
		return getTypeName();
	}
}
