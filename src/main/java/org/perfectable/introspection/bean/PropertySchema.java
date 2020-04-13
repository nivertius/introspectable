package org.perfectable.introspection.bean;

import org.perfectable.introspection.type.TypeView;

import java.lang.reflect.Type;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;


public abstract class PropertySchema<B, T> {

	public abstract String name();

	public abstract Type type();

	public abstract boolean isReadable();

	public abstract boolean isWritable();

	final Property<B, T> bind(B bean) {
		return Property.of(bean, this);
	}

	PropertySchema() {
		// package-only inheritance
	}

	abstract T get(B bean);

	abstract void set(B bean, @Nullable T value);

	final <X extends T> PropertySchema<B, X> as(Class<X> propertyClass) {
		checkArgument(TypeView.of(type()).isSuperTypeOf(propertyClass));
		@SuppressWarnings("unchecked")
		PropertySchema<B, X> casted = (PropertySchema<B, X>) this;
		return casted;
	}


}
