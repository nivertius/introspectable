package org.perfectable.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class Fields {

	public static boolean isGettable(Field field) {
		return field.isAccessible();
	}

	public static boolean isSettable(Field field) {
		return field.isAccessible() && !Modifier.isFinal(field.getModifiers());
	}

	public static boolean isStatic(Field field) {
		return Modifier.isStatic(field.getModifiers());
	}

	private Fields() {
	}
}
