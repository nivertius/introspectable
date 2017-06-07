package org.perfectable.introspection;

import java.lang.reflect.Method;

public final class Introspections {

	public static <X> ClassIntrospection<X> introspect(Class<X> type) {
		return ClassIntrospection.of(type);
	}

	public static MethodIntrospection introspect(Method method) {
		return MethodIntrospection.of(method);
	}

	public static ClassLoaderIntrospection introspect(ClassLoader classLoader) {
		return ClassLoaderIntrospection.of(classLoader);
	}

	private Introspections() {
		// utility
	}
}
