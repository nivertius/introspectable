package org.perfectable.introspection.bean;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.perfectable.introspection.Introspections.introspect;

/**
 * Schema of a Java Bean.
 *
 * <p>This class represents view of a java class from perspective of Java Beans. This means that instead of class
 * fields, methods, constructors and so on, bean schema has type and {@link PropertySchema}, and can be instantiated,
 *
 * @param <B> class that this schema covers
 */
public final class BeanSchema<B extends @NonNull Object> {
	private final Class<B> beanClass;

	/**
	 * Creates schema from provided class.
	 *
	 * @param beanClass class to create schema from
	 * @param <X> type of schema
	 * @return Bean schema for provided class
	 */
	public static <X extends @NonNull Object> BeanSchema<X> from(Class<X> beanClass) {
		requireNonNull(beanClass);
		return new BeanSchema<>(beanClass);
	}

	private BeanSchema(Class<B> beanClass) {
		this.beanClass = beanClass;
	}

	/**
	 * Inserts bean instance into this schema, creating actual {@link Bean}.
	 *
	 * @param element element to convert to bean
	 * @return bean from the element
	 */
	public Bean<B> put(@NonNull B element) {
		checkArgument(this.beanClass.isInstance(element));
		return Bean.from(element);
	}

	/**
	 * Class backing this schema.
	 *
	 * @return schema class
	 */
	public Class<B> type() {
		return beanClass;
	}

	/**
	 * Creates new empty bean from this schema.
	 *
	 * <p>This will actually invoke parameterless constructor of the class.
	 *
	 * @return new bean from this schema
	 */
	public Bean<B> instantiate() {
		B instance = introspect(beanClass).instantiate();
		return Bean.from(instance);
	}

	/**
	 * Extracts property schema by name.
	 *
	 * @param name name of the schema searched
	 * @return property schema for provided name
	 * @throws IllegalArgumentException when property with this name doesn't exist in bean schema.
	 */
	public PropertySchema<B, ?> property(String name) {
		return Properties.create(this.beanClass, name);
	}

	@Override
	public boolean equals(@Nullable Object obj) {
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
