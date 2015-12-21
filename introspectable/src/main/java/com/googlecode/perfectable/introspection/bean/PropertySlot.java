package com.googlecode.perfectable.introspection.bean;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.googlecode.perfectable.introspection.Methods;

public final class PropertySlot<CT, PT> {
	
	private final String propertyName;
	private final Class<PT> propertyClass;
	
	private PropertySlot(@SuppressWarnings("unused") Class<? extends CT> beanClass, String propertyName,
			Class<PT> propertyClass) {
		this.propertyName = propertyName;
		this.propertyClass = propertyClass;
	}
	
	public static <CX, PX> PropertySlot<CX, PX> from(Class<? extends CX> beanClass, String propertyName,
			Class<PX> propertyClass) {
		return new PropertySlot<>(beanClass, propertyName, propertyClass);
	}
	
	public static <CX> PropertySlot<CX, ?> raw(Class<? extends CX> beanClass, String propertyName) {
		return new PropertySlot<>(beanClass, propertyName, Object.class);
	}
	
	public Property<CT, PT> put(CT target) {
		return Bean.from(target).property(this.propertyName, this.propertyClass);
	}
	
	public static PropertySlot<?, ?> from(Field field) {
		final Class<?> declaringClass = field.getDeclaringClass();
		final Class<?> type = field.getType();
		return from(declaringClass, field.getName(), type);
	}
	
	public static PropertySlot<?, ?> from(Method setter) {
		checkArgument(Methods.isSetter(setter));
		final Class<?> declaringClass = setter.getDeclaringClass();
		final Class<?> type = setter.getReturnType();
		// MARK name
		return from(declaringClass, setter.getName(), type);
	}
	
}
