package org.perfectable.introspection.bean;

import java.lang.reflect.Type;
import java.util.Objects;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

public final class Property<B, T> {
	private final B bean;
	private final PropertySchema<B, T> schema;

	static <B, T> Property<B, T> of(B bean, PropertySchema<B, T> schema) {
		requireNonNull(bean);
		requireNonNull(schema);
		return new Property<>(bean, schema);
	}

	private Property(B bean, PropertySchema<B, T> schema) {
		this.bean = bean;
		this.schema = schema;
	}

	public <X extends T> Property<B, X> as(Class<X> propertyClass) {
		return schema.as(propertyClass).bind(bean);
	}

	@Nullable
	public T get() {
		return schema.get(this.bean);
	}

	public void set(@Nullable T value) {
		schema.set(bean, value);
	}

	public Type type() {
		return schema.type();
	}

	public String name() {
		return schema.name();
	}

	public boolean isReadable() {
		return schema.isReadable();
	}

	public boolean isWritable() {
		return schema.isWritable();
	}

	public void copy(Bean<B> other) {
		T value = get();
		this.schema.set(other.contents(), value);
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Property<?, ?>)) {
			return false;
		}
		Property<?, ?> other = (Property<?, ?>) obj;
		return Objects.equals(schema, other.schema)
			&& Objects.equals(bean, other.bean);
	}

	@Override
	public int hashCode() {
		return Objects.hash(schema, bean);
	}
}
