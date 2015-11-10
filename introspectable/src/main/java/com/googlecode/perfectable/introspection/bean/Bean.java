package com.googlecode.perfectable.introspection.bean;

import java.lang.reflect.Modifier;
import java.util.stream.Stream;

import com.googlecode.perfectable.introspection.Copier;
import com.googlecode.perfectable.introspection.query.FieldQuery;

public class Bean<T> {
	
	private final T bean;
	
	private Bean(T bean) {
		this.bean = bean;
	}
	
	public static <X> Bean<X> from(X entity) {
		return new Bean<>(entity);
	}
	
	public T copy() {
		return Copier.copy(this.bean);
	}
	
	public Stream<?> related() {
		return this.fieldProperties()
				.map(Property::get)
				.filter(related -> related != null);
	}
	
	public Stream<Property<T, ?>> fieldProperties() {
		return FieldQuery.of(this.bean.getClass())
				.excludingModifier(Modifier.STATIC)
				.stream()
				.map(field -> Property.<T> from(this.bean, field));
	}
	
	public <X> Property<T, X> property(String name, Class<X> type) {
		return Property.from(this.bean, name, type);
	}
	
	public Property<T, Object> property(String name) {
		return Property.raw(this.bean, name);
	}
	
}
