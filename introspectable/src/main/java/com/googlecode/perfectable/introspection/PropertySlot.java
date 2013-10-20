package com.googlecode.perfectable.introspection;

public final class PropertySlot<CT, PT> {

	private final String propertyName;
	private final Class<PT> propertyClass;

	private PropertySlot(@SuppressWarnings("unused") Class<CT> beanClass, String propertyName, Class<PT> propertyClass) {
		this.propertyName = propertyName;
		this.propertyClass = propertyClass;
	}

	public static <CX, PX> PropertySlot<CX, PX> from(Class<CX> beanClass, String propertyName, Class<PX> propertyClass) {
		return new PropertySlot<>(beanClass, propertyName, propertyClass);
	}

	public static <CX> PropertySlot<CX, Object> raw(Class<CX> beanClass, String propertyName) {
		return new PropertySlot<>(beanClass, propertyName, Object.class);
	}

	public Property<CT, PT> put(CT target) {
		return Bean.from(target).property(this.propertyName, this.propertyClass);
	}

}
