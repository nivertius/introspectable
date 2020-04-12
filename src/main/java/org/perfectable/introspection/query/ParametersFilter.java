package org.perfectable.introspection.query;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

@FunctionalInterface
public interface ParametersFilter {
	static ParametersFilter typesAccepted(Type... parameterTypes) {
		return ParameterFilters.Types.accepting(parameterTypes);
	}

	static ParametersFilter typesExact(Type... parameterTypes) {
		return ParameterFilters.Types.exact(parameterTypes);
	}

	static ParametersFilter matchingArguments(Object... arguments) {
		return ParameterFilters.matchingArguments(arguments);
	}

	static ParametersFilter count(int count) {
		return ParameterFilters.Count.of(count);
	}

	boolean matches(Parameter[] parameters, boolean varArgs);

	default boolean matches(Executable executable) {
		return matches(executable.getParameters(), executable.isVarArgs());
	}

}
