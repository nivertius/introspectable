package org.perfectable.introspection.query;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Filter for method or constructor parameters.
 *
 * <p>Default filters created with static methods deal with variable arity executables in a expected way - they check
 * if call could be called with specified arguments from equivalent source code call.
 */
@FunctionalInterface
public interface ParametersFilter {
	/**
	 * Creates filter that matches when specified types can be used as a arguments for tested executable.
	 *
	 * @param parameterTypes types of arguments that would be passed to executable
	 * @return filter that checks if the executable would accept specified argument types
	 */
	static ParametersFilter typesAccepted(Type... parameterTypes) {
		return ParameterFilters.Types.accepting(parameterTypes);
	}

	/**
	 * Creates filter that matches when executable has exactly the provided parameters.
	 *
	 * @param parameterTypes types that are checked to be exactly parameters of executable
	 * @return filter that checks if the executable has exactly specified types of parameters
	 */
	static ParametersFilter typesExact(Type... parameterTypes) {
		return ParameterFilters.Types.exact(parameterTypes);
	}

	/**
	 * Creates filter that checks if specified arguments by thier value could be passed to checked executable.
	 *
	 * @param arguments arguments that would be checked for passing to executable
	 * @return filter that checks if the executable would accept specified arguments
	 */
	static ParametersFilter matchingArguments(@Nullable Object... arguments) {
		return ParameterFilters.matchingArguments(arguments);
	}

	/**
	 * Creates a filter that checks if executable could be called with the specified number of arguments.
	 *
	 * @param count number of arguments to check executable against
	 * @return filter that checks if executable can be called with specified count of arguments
	 */
	static ParametersFilter count(int count) {
		return ParameterFilters.Count.of(count);
	}

	/**
	 * Matches parameters of an executable against this filter.
	 *
	 * @param parameters parameters that checked executable has
	 * @param varArgs if the executable is variable arity
	 * @return if the parameters are accepted by this filter
	 */
	boolean matches(Parameter[] parameters, boolean varArgs);

	/**
	 * Matches executable for parameters.
	 *
	 * @param executable executable to test
	 * @return if the executable has parameters that are required by this filter
	 */
	default boolean matches(Executable executable) {
		return matches(executable.getParameters(), executable.isVarArgs());
	}

}
