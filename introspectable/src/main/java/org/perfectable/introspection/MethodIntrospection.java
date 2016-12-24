package org.perfectable.introspection;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class MethodIntrospection {
	static MethodIntrospection of(Method method) {
		return new MethodIntrospection(method);
	}

	private final Method method;

	public boolean isCallable() {
		return method.isAccessible() && !Modifier.isAbstract(method.getModifiers());
	}

	private MethodIntrospection(Method method) {
		this.method = method;
	}
}
