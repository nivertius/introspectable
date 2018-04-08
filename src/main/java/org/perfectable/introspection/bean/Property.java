package org.perfectable.introspection.bean;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class Property<CT, PT> {

	public abstract String name();

	public abstract Class<PT> type();

	public abstract boolean isReadable();

	public abstract boolean isWritable();

	public BoundProperty<CT, PT> bind(CT bean) {
		return BoundProperty.of(bean, this);
	}

	Property() {
		// package-only inheritance
	}

	abstract PT get(CT bean);

	abstract void set(CT bean, @Nullable PT value);

	<X extends PT> Property<CT, X> as(Class<X> propertyClass) {
		checkArgument(propertyClass.isAssignableFrom(type()));
		@SuppressWarnings("unchecked")
		Property<CT, X> casted = (Property<CT, X>) this;
		return casted;
	}


}
