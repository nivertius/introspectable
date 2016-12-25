package org.perfectable.introspection.query;

import java.lang.reflect.Parameter;

public class TypeParametersFilter implements ParametersFilter {
	private final Class<?>[] parameterTypes;

	TypeParametersFilter(Class<?>... parameterTypes) {
		this.parameterTypes = parameterTypes.clone();
	}

	@Override
	public boolean matches(Parameter... parameters) {
		if (this.parameterTypes.length != parameters.length) {
			return false;
		}
		for (int i = 0; i < this.parameterTypes.length; i++) {
			Class<?> expectedParameterType = this.parameterTypes[i];
			Class<?> actualParameterType = parameters[i].getType();
			if (!expectedParameterType.isAssignableFrom(actualParameterType)) {
				return false;
			}
		}
		return true;

	}
}
