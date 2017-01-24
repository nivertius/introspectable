package org.perfectable.introspection.query;

import java.lang.reflect.Parameter;

@FunctionalInterface
public interface ParametersFilter {
	static ParametersFilter types(Class<?>... parameterTypes) {
		return new TypeParametersFilter(parameterTypes);
	}

	boolean matches(Parameter... parameters);

}
