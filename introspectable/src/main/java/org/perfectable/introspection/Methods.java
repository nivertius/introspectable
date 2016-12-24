package org.perfectable.introspection;

import java.lang.reflect.Method;

import static org.perfectable.introspection.Introspections.introspect;

public final class Methods {

	public static final Method OBJECT_EQUALS =
			introspect(Object.class).methods().named("equals").parameters(Object.class).single();
	public static final Method OBJECT_TO_STRING =
			introspect(Object.class).methods().named("toString").parameters().single();
	public static final Method OBJECT_FINALIZE =
			introspect(Object.class).methods().named("finalize").parameters().single();

	private Methods() {
	}

}
