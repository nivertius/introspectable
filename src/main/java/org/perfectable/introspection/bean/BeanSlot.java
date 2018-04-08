package org.perfectable.introspection.bean;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.perfectable.introspection.Introspections.introspect;

public final class BeanSlot<T> {
	private final Class<T> beanClass;

	public static <X> BeanSlot<X> from(Class<X> beanClass) {
		requireNonNull(beanClass);
		return new BeanSlot<>(beanClass);
	}

	private BeanSlot(Class<T> beanClass) {
		this.beanClass = beanClass;
	}

	public Bean<T> put(T element) {
		checkArgument(this.beanClass.isInstance(element));
		return Bean.from(element);
	}

	public Class<T> type() {
		return beanClass;
	}

	public Bean<T> instantiate() {
		T instance = introspect(beanClass).instantiate();
		return Bean.from(instance);
	}

	public Property<T, ?> property(String name) {
		return Properties.create(this.beanClass, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BeanSlot<?>)) {
			return false;
		}
		BeanSlot<?> other = (BeanSlot<?>) obj;
		return beanClass.equals(other.beanClass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(beanClass);
	}
}
