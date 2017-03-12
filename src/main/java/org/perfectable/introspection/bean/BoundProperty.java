package org.perfectable.introspection.bean;

import javax.annotation.Nullable;

public final class BoundProperty<CT, PT> {
	private final CT bean;
	private final Property<CT, PT> property;

	public static <CT, PT> BoundProperty<CT, PT> of(CT bean, Property<CT, PT> property) {
		return new BoundProperty<>(bean, property);
	}

	private BoundProperty(CT bean, Property<CT, PT> property) {
		this.bean = bean;
		this.property = property;
	}

	@Nullable
	public PT get() {
		return property.get(this.bean);
	}

	public void set(@Nullable PT value) {
		property.set(bean, value);
	}

	public void copy(CT other) {
		PT value = get();
		this.property.set(other, value);
	}
}
