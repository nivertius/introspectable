package org.perfectable.introspection.bean;

import org.perfectable.introspection.query.FieldQuery;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a view of an instance as its property values, typically containing a proper Java Bean.
 *
 * <p>Instances are created using {@link #from}.
 *
 * <p>Allows access to properties either by name (using {@link #property}) or by listing them
 * (eg. {@link #fieldProperties}).
 * Properties are defined as either getter and/or setter or field.
 *
 * @param <B> type of instance contained in this bean
 */
public final class Bean<B extends @NonNull Object> {

	private final B instance;

	private Bean(B instance) {
		this.instance = instance;
	}

	/**
	 * Creates bean from Java Bean object.
	 *
	 * @param instance a non-null object that this bean will allow introspection into
	 * @param <X> Instance type
	 * @return Bean containing specified instance
	 */
	public static <X extends @NonNull Object> Bean<X> from(X instance) {
		return new Bean<>(instance);
	}

	/**
	 * Extracts instance that is backing this bean.
	 *
	 * @return instance that is backing this bean.
	 */
	public B contents() {
		return instance;
	}

	/**
	 * Extracts actual type of backing instance.
	 *
	 * @return actual type of backing instance.
	 */
	@SuppressWarnings("unchecked")
	public Class<B> type() {
		return (Class<B>) this.instance.getClass();
	}

	/**
	 * Extracts schema of this bean.
	 *
	 * @return schema of this bean
	 */
	public BeanSchema<B> schema() {
		return BeanSchema.from(type());
	}

	/**
	 * Finds property by name.
	 *
	 * @param name name of the property to look for.
	 * @return property with provided name
	 * @throws IllegalArgumentException when there is no property with provided name
	 */
	public Property<B, @Nullable Object> property(String name) {
		return Properties.create(type(), name).bind(this.instance);
	}

	/**
	 * Creates new bean with contained instance copied field-by-field from this bean backing instance.
	 *
	 * <p>Copy is created by default constructor.
	 *
	 * @return copy of this bean.
	 */
	public Bean<B> copy() {
		Bean<B> duplicate = BeanSchema.from(type()).instantiate();
		fieldProperties()
				.forEach(property -> property.copy(duplicate));
		return duplicate;
	}

	/**
	 * Lists all properties that are backed by a field.
	 *
	 * @return all properties backed by a field.
	 */
	public ImmutableSet<Property<B, @Nullable Object>> fieldProperties() {
		return FieldQuery.of(type())
			.excludingModifier(Modifier.STATIC)
			.stream()
			.map((Function<Field, PropertySchema<B, @Nullable Object>>) Properties::fromField)
			.map((Function<PropertySchema<B, @Nullable Object>, Property<B, @Nullable Object>>)
				schema -> schema.bind(instance))
			.collect(ImmutableSet.toImmutableSet());
	}

	/**
	 * Finds all object that backing instance has reference to.
	 *
	 * @return all objects that backing instance has reference to.
	 */
	public ImmutableSet<Object> related() {
		return this.fieldProperties().stream()
				.map(Property::getAsOptional)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public boolean equals(@Nullable Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Bean<?>)) {
			return false;
		}
		Bean<?> other = (Bean<?>) obj;
		return instance.equals(other.instance);
	}

	@Override
	public int hashCode() {
		return Objects.hash(instance);
	}
}
