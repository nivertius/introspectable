package org.perfectable.introspection.bean;

import org.perfectable.introspection.query.FieldQuery;

import java.lang.reflect.Modifier;
import java.util.Objects;

import com.google.common.collect.ImmutableSet;

import static java.util.Objects.requireNonNull;

public final class Bean<B> {

	private final B instance;

	private Bean(B instance) {
		this.instance = instance;
	}

	public static <X> Bean<X> from(X instance) {
		requireNonNull(instance);
		return new Bean<>(instance);
	}

	public B contents() {
		return instance;
	}

	@SuppressWarnings("unchecked")
	public Class<B> type() {
		return (Class<B>) this.instance.getClass();
	}

	public Property<B, Object> property(String name) {
		return Properties.create(type(), name).bind(this.instance);
	}

	public Bean<B> copy() {
		Bean<B> duplicate = BeanSchema.from(type()).instantiate();
		fieldProperties()
				.forEach(property -> property.copy(duplicate));
		return duplicate;
	}

	public ImmutableSet<Property<B, ?>> fieldProperties() {
		return FieldQuery.of(type())
			.excludingModifier(Modifier.STATIC)
			.stream()
			.map(field -> Properties.<B>fromField(field).bind(this.instance))
			.collect(ImmutableSet.toImmutableSet());
	}

	public ImmutableSet<Object> related() {
		return this.fieldProperties().stream()
				.map(Property::get)
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
