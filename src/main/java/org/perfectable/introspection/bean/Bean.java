package org.perfectable.introspection.bean;

import org.perfectable.introspection.query.FieldQuery;

import java.lang.reflect.Modifier;
import java.util.Objects;

import com.google.common.collect.ImmutableSet;

import static java.util.Objects.requireNonNull;
import static org.perfectable.introspection.Introspections.introspect;

public final class Bean<T> {

	private final T instance;

	private Bean(T instance) {
		this.instance = instance;
	}

	public static <X> Bean<X> from(X instance) {
		requireNonNull(instance);
		return new Bean<>(instance);
	}

	public T contents() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	public Class<T> type() {
		return (Class<T>) this.instance.getClass();
	}

	public BoundProperty<T, Object> property(String name) {
		return Properties.create(type(), name).bind(this.instance);
	}

	public Bean<T> copy() {
		T duplicate = introspect(type()).instantiate();
		fieldProperties()
				.forEach(property -> property.copy(duplicate));
		return new Bean<>(duplicate);
	}

	public ImmutableSet<BoundProperty<T, ?>> fieldProperties() {
		return FieldQuery.of(type())
			.excludingModifier(Modifier.STATIC)
			.stream()
			.map(field -> Properties.<T>fromField(field).bind(this.instance))
			.collect(ImmutableSet.toImmutableSet());
	}

	public ImmutableSet<Object> related() {
		return this.fieldProperties().stream()
				.map(BoundProperty::get)
				.filter(Objects::nonNull)
				.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public boolean equals(Object obj) {
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
