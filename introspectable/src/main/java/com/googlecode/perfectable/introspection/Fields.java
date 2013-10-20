package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.annotation.Nullable;

public class Fields {

	@Nullable
	public static Field find(Class<?> beanClass, String name, Class<?> type) {
		Field field = find(beanClass, name);
		if(field == null) {
			return null;
		}
		checkState(type.isAssignableFrom(field.getType()));
		return field;
	}

	@Nullable
	public static Field find(Class<?> sourceClass, String name) {
		for(Class<?> currentClass : InheritanceChain.startingAt(sourceClass)) {
			try {
				return currentClass.getDeclaredField(name);
			}
			catch(NoSuchFieldException e) {
				// continue the search
			}
			catch(SecurityException e) {
				throw new RuntimeException(e); // TODO Auto-generated catch block
			}
		}
		return null;
	}

	public static boolean isSettable(Field field) {
		return !Modifier.isFinal(field.getModifiers());
	}

	public static boolean isStatic(Field field) {
		return Modifier.isStatic(field.getModifiers());
	}

}
