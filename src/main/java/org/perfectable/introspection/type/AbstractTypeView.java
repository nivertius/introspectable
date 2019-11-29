package org.perfectable.introspection.type;

import java.lang.reflect.Type;

abstract class AbstractTypeView<T extends Type> extends TypeView {
	protected final T type;

	AbstractTypeView(T type) {
		this.type = type;
	}

	@Override
	public final T unwrap() {
		return type;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!getClass().isInstance(obj)) {
			return false;
		}
		AbstractTypeView<?> other = (AbstractTypeView<?>) obj;
		return type.equals(other.type);
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public String toString() {
		return "View(" + type + ")";
	}
}
