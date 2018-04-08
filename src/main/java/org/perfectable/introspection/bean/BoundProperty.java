package org.perfectable.introspection.bean;

import java.util.Objects;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class BoundProperty<B, T> {
	private final B bean;
	private final Property<B, T> property;

	static <CT, PT> BoundProperty<CT, PT> of(CT bean, Property<CT, PT> property) {
		requireNonNull(bean);
		requireNonNull(property);
		return new BoundProperty<>(bean, property);
	}

	private BoundProperty(B bean, Property<B, T> property) {
		this.bean = bean;
		this.property = property;
	}

	public <X extends T> BoundProperty<B, X> as(Class<X> propertyClass) {
		return property.as(propertyClass).bind(bean);
	}

	@Nullable
	public T get() {
		return property.get(this.bean);
	}

	public void set(@Nullable T value) {
		property.set(bean, value);
	}

	public Class<T> type() {
		return property.type();
	}

	public String name() {
		return property.name();
	}

	public boolean isReadable() {
		return property.isReadable();
	}

	public boolean isWritable() {
		return property.isWritable();
	}

	public void copy(B other) {
		T value = get();
		this.property.set(other, value);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BoundProperty<?, ?>)) {
			return false;
		}
		BoundProperty<?, ?> other = (BoundProperty<?, ?>) obj;
		return Objects.equals(property, other.property)
			&& Objects.equals(bean, other.bean);
	}

	@Override
	public int hashCode() {
		return Objects.hash(property, bean);
	}
}
