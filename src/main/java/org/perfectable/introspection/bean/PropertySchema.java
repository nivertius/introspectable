package org.perfectable.introspection.bean;

import org.perfectable.introspection.type.TypeView;

import java.lang.reflect.Type;

import org.checkerframework.checker.nullness.qual.NonNull;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Schema of a Java Bean property.
 *
 * <p>This class represents view of a field or getter/setter from perspective of Java Beans. This means that instead of
 * fields and methods, property schema schema has type, name, and can be read and written.
 *
 * @param <B> bean class for this property
 * @param <T> type of property values
 */
public abstract class PropertySchema<B extends @NonNull Object, T> {
	/**
	 * Extracts name of the property.
	 *
	 * @return property name
	 */
	public abstract String name();

	/**
	 * Extracts type of the property.
	 *
	 * @return property type
	 */
	public abstract Type type();

	/**
	 * Answers if the property is readable.
	 *
	 * @return if the property can have value read
	 */
	public abstract boolean isReadable();

	/**
	 * Answers if the property is writeable.
	 *
	 * @return if the property can have value written
	 */
	public abstract boolean isWritable();

	final Property<B, T> bind(B bean) {
		return Property.of(bean, this);
	}

	PropertySchema() {
		// package-only inheritance
	}

	abstract T get(B bean);

	abstract void set(B bean, T value);

	final <X extends T> PropertySchema<B, X> as(Class<X> propertyClass) {
		checkArgument(TypeView.of(type()).isSuperTypeOf(propertyClass));
		@SuppressWarnings("unchecked")
		PropertySchema<B, X> casted = (PropertySchema<B, X>) this;
		return casted;
	}


}
