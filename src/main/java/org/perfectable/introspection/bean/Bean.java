package org.perfectable.introspection.bean;

import org.perfectable.introspection.query.FieldQuery;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.stream.Stream;

import static org.perfectable.introspection.Introspections.introspect;

public final class Bean<T> {

	private final T instance;

	private Bean(T instance) {
		this.instance = instance;
	}

	public static <X> Bean<X> from(X instance) {
		return new Bean<>(instance);
	}

	public T copy() {
		T duplicate = introspect(type()).instantiate();
		fieldProperties()
				.forEach(property -> property.copy(duplicate));
		return duplicate;
	}

	public Stream<?> related() {
		return this.fieldProperties()
				.map(BoundProperty::get)
				.filter(Objects::nonNull);
	}

	@SuppressWarnings("unchecked")
	public Class<T> type() {
		return (Class<T>) this.instance.getClass();
	}

	public Stream<BoundProperty<T, ?>> fieldProperties() {
		Class<T> instanceClass = type();
		return FieldQuery.of(this.instance.getClass())
				.excludingModifier(Modifier.STATIC)
				.stream()
				.map(field -> Property.fromField(field, instanceClass).bind(this.instance));
	}

	public <X> BoundProperty<T, X> property(String name, Class<X> type) {
		return Property.from(type(), name, type).bind(this.instance);
	}

	public BoundProperty<T, Object> property(String name) {
		return Property.raw(type(), name).bind(this.instance);
	}

}
