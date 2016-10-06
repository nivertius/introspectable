package org.perfectable.introspection.injection;

import org.perfectable.introspection.bean.Property;

final class PropertyInjection<T, X> implements Injection<T> {
	private final Property<T, X> property;
	private final X value;

	public static <T, X> PropertyInjection<T, X> create(Property<T, X> property, X value) {
		return new PropertyInjection<T, X>(property, value);
	}

	private PropertyInjection(Property<T, X> property, X value) {
		this.property = property;
		this.value = value;
	}

	@Override
	public void perform(T target) {
		this.property.bind(target).set(this.value);
	}
}
