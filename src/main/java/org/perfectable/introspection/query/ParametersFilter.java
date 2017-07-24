package org.perfectable.introspection.query;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;

@FunctionalInterface
public interface ParametersFilter {
	static ParametersFilter typesAccepted(Class<?>... parameterTypes) {
		return ParameterFilters.Types.accepting(parameterTypes);
	}

	static ParametersFilter typesExact(Class<?>... parameterTypes) {
		return ParameterFilters.Types.exact(parameterTypes);
	}

	static ParametersFilter count(int count) {
		return ParameterFilters.Count.of(count);
	}

	boolean matches(Parameter[] parameters, boolean varArgs);

	default boolean matches(Executable executable) {
		return matches(executable.getParameters(), executable.isVarArgs());
	}

}
