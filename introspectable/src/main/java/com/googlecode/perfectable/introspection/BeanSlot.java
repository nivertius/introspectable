package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkState;

public class BeanSlot<T> {
	private final Class<T> beanClass;

	public BeanSlot(Class<T> beanClass) {
		this.beanClass = beanClass;
	}

	public static <X> BeanSlot<X> from(Class<X> beanClass) {
		return new BeanSlot<>(beanClass);
	}

	public Bean<T> put(T element) {
		checkState(this.beanClass.isInstance(element));
		return Bean.from(element);
	}

	public <X> PropertySlot<T, X> property(String name, Class<X> type) {
		return PropertySlot.from(this.beanClass, name, type);
	}

	public PropertySlot<T, Object> property(String name) {
		return PropertySlot.raw(this.beanClass, name);
	}
}
