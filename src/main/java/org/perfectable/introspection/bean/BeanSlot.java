package org.perfectable.introspection.bean;

import static com.google.common.base.Preconditions.checkState;

public final class BeanSlot<T> {
	private final Class<T> beanClass;

	public static <X> BeanSlot<X> from(Class<X> beanClass) {
		return new BeanSlot<>(beanClass);
	}

	private BeanSlot(Class<T> beanClass) {
		this.beanClass = beanClass;
	}

	public Bean<T> put(T element) {
		checkState(this.beanClass.isInstance(element));
		return Bean.from(element);
	}

	public <X> Property<T, X> property(String name, Class<X> type) {
		return Property.from(this.beanClass, name, type);
	}

	public Property<T, ?> property(String name) {
		return Property.raw(this.beanClass, name);
	}
}
