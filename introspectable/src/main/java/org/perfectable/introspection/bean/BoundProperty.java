package org.perfectable.introspection.bean;

public class BoundProperty<CT, PT> {
	private final Property<CT, PT> property;
	private final CT bean;

	public BoundProperty(Property<CT, PT> property, CT bean) {
		this.property = property;
		this.bean = bean;
	}

	public PT get() {
		return property.get(this.bean);
	}

	public void set(PT value) {
		property.set(bean, value);
	}

	public void copy(CT other) {
		PT value = get();
		this.property.set(other, value);
	}
}
