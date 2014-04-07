package com.googlecode.perfectable.introspection;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.LinkedList;

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

	public Iterable<Object> related() {
		Collection<Object> result = new LinkedList<>();
		for(Property<T, ?> property : this.fieldProperties()) {
			Object related = property.get();
			if(related != null) {
				result.add(related);
			}
		}
		return result;
	}

	Iterable<Property<T, ?>> fieldProperties() {
		Collection<Property<T, ?>> result = new LinkedList<>();
		Class<?> beanClass = this.bean.getClass();
		for(Class<?> currentClass : InheritanceChain.startingAt(beanClass)) {
			for(Field field : currentClass.getDeclaredFields()) {
				if(Fields.isStatic(field)) {
					continue;
				}
				result.add(Property.from(this.bean, field));
			}
		}
		return result;
	}

	public <X> Property<T, X> property(String name, Class<X> type) {
		return Property.from(this.bean, name, type);
	}

	public Property<T, Object> property(String name) {
		return Property.raw(this.bean, name);
	}

}
