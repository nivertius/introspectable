package org.perfectable.introspection.bean;

import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.perfectable.introspection.Introspections.introspect;

public final class BeanSchema<B> {
	private final Class<B> beanClass;

	public static <X> BeanSchema<X> from(Class<X> beanClass) {
		requireNonNull(beanClass);
		return new BeanSchema<>(beanClass);
	}

	private BeanSchema(Class<B> beanClass) {
		this.beanClass = beanClass;
	}

	public Bean<B> put(B element) {
		checkArgument(this.beanClass.isInstance(element));
		return Bean.from(element);
	}

	public Class<B> type() {
		return beanClass;
	}

	public Bean<B> instantiate() {
		B instance = introspect(beanClass).instantiate();
		return Bean.from(instance);
	}

	public PropertySchema<B, ?> property(String name) {
		return Properties.create(this.beanClass, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BeanSchema<?>)) {
			return false;
		}
		BeanSchema<?> other = (BeanSchema<?>) obj;
		return beanClass.equals(other.beanClass);
	}

	@Override
	public int hashCode() {
		return Objects.hash(beanClass);
	}
}
