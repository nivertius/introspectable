package org.perfectable.introspection.injection;

import org.perfectable.introspection.bean.Property;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@FunctionalInterface
public interface Injection<T> {

	static <TX> Injection<TX> create(Field field, Object value) {
		@SuppressWarnings("unchecked")
		Property<TX, Object> property = (Property<TX, Object>) Property.fromField(field);
		return create(property, value);
	}

	static <TX> Injection<TX> create(Method setter, Object value) {
		@SuppressWarnings("unchecked")
		Property<TX, Object> property = (Property<TX, Object>) Property.fromSetter(setter);
		return create(property, value);
	}

	static <TX, PX> Injection<TX> create(Property<TX, PX> property, PX value) {
		return PropertyInjection.create(property, value);
	}

	static <TX, PX> Injection<TX> create(Class<TX> beanType, String propertyName, Class<PX> propertyType,
										 PX value) {
		Property<TX, PX> property = Property.from(beanType, propertyName, propertyType);
		return create(property, value);
	}

	void perform(T target);

	default <X extends T> Injection<X> andThen(Injection<? super X> next) {
		return CompositeInjection.create(this, next);
	}

}
