package com.googlecode.perfectable.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.annotation.Nullable;

public class Fields {
	
	@Nullable
	@Deprecated
	public static Field find(Class<?> beanClass, String name, Class<?> type) {
		return Introspection.of(beanClass).fields().named(name).typed(type).single();
	}
	
	@Nullable
	@Deprecated
	public static Field find(Class<?> sourceClass, String name) {
		return Introspection.of(sourceClass).fields().named(name).single();
	}
	
	public static boolean isGettable(Field field) {
		return field.isAccessible();
	}
	
	public static boolean isSettable(Field field) {
		return field.isAccessible() && !Modifier.isFinal(field.getModifiers());
	}
	
	public static boolean isStatic(Field field) {
		return Modifier.isStatic(field.getModifiers());
	}
	
}
