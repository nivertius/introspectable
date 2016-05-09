package org.perfectable.introspection.bean;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import org.perfectable.introspection.Copier;
import org.perfectable.introspection.query.FieldQuery;

public final class Bean<T> {
	
	private final T instance;
	
	private Bean(T instance) {
		this.instance = instance;
	}
	
	public static <X> Bean<X> from(X instance) {
		return new Bean<>(instance);
	}
	
	public T copy() {
		return Copier.copy(this.instance);
	}
	
	public Stream<?> related() {
		return this.fieldProperties()
				.map(Property::get)
				.filter(related -> related != null);
	}
	
	public Stream<Property<T, ?>> fieldProperties() {
		return FieldQuery.of(this.instance.getClass())
				.excludingModifier(Modifier.STATIC)
				.stream()
				.map(field -> Property.from(this.instance, field));
	}
	
	public <X> Property<T, X> property(String name, Class<X> type) {
		return Property.from(this.instance, name, type);
	}
	
	public Property<T, Object> property(String name) {
		return Property.raw(this.instance, name);
	}
	
}
