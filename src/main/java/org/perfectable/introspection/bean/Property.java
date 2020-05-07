package org.perfectable.introspection.bean;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Represents a property of a bean.
 *
 * @param <B> Bean type that has this property
 * @param <T> Type of property value
 */
public final class Property<B extends @NonNull Object, T> {
	private final B bean;
	private final PropertySchema<B, T> schema;

	static <B extends @NonNull Object, T> Property<B, T> of(B bean, PropertySchema<B, T> schema) {
		requireNonNull(bean);
		requireNonNull(schema);
		return new Property<>(bean, schema);
	}

	private Property(B bean, PropertySchema<B, T> schema) {
		this.bean = bean;
		this.schema = schema;
	}

	/**
	 * Safely casts expected value to specific type.
	 *
	 * @param newValueType new value type representation
	 * @param <X> new value type
	 * @return property with new value type
	 * @throws IllegalArgumentException if property value aren't of this type.
	 */
	public <X extends T> Property<B, X> as(Class<X> newValueType) {
		return schema.as(newValueType).bind(bean);
	}

	/**
	 * Extracts value of this property from bound bean.
	 *
	 * @return Property value
	 * @throws IllegalStateException when this property is not readable
	 */
	public T get() {
		return schema.get(this.bean);
	}

	/**
	 * Extracts value of this property as a non-null optional.
	 *
	 * @return Property value, empty if property is null
	 * @throws IllegalStateException when this property is not readable
	 */
	public Optional<@NonNull T> getAsOptional() {
		return Optional.ofNullable(get());
	}

	/**
	 * Sets provided value for this property of bound bean.
	 *
	 * @param value new value for this property
	 * @throws IllegalStateException when this property is not writeable
	 */
	public void set(T value) {
		schema.set(bean, value);
	}

	/**
	 * Extracts type of the property.
	 *
	 * @return property type
	 */
	public Type type() {
		return schema.type();
	}

	/**
	 * Extracts name of the property.
	 *
	 * @return property name
	 */
	public String name() {
		return schema.name();
	}

	/**
	 * Answers if the property is readable, i.e. {@link #get} will succeed.
	 *
	 * @return if the property can have value read
	 */
	public boolean isReadable() {
		return schema.isReadable();
	}

	/**
	 * Answers if the property is writeable, i.e. {@link #set} will succeed.
	 *
	 * @return if the property can have value written
	 */
	public boolean isWritable() {
		return schema.isWritable();
	}

	/**
	 * Copies value of this property to same property of another bean.
	 *
	 * @param other bean to set value on
	 * @throws IllegalStateException when this property is either not readable or not writeable
	 */
	public void copy(Bean<B> other) {
		@Nullable T value = get();
		this.schema.set(other.contents(), value);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
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
