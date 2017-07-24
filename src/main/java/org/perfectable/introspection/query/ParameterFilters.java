package org.perfectable.introspection.query;

import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

final class ParameterFilters {
	static final class Types implements ParametersFilter {
		private final ImmutableList<TypeFilter> parameterTypes;

		static Types accepting(Class<?>... parameterTypes) {
			ImmutableList<TypeFilter> typeFilters =
				Stream.of(parameterTypes).map(TypeFilter::superTypeOf).collect(toImmutableList());
			return new Types(typeFilters);
		}

		static Types exact(Class<?>... parameterTypes) {
			ImmutableList<TypeFilter> typeFilters =
				Stream.of(parameterTypes).map(TypeFilter::exact).collect(toImmutableList());
			return new Types(typeFilters);
		}

		private Types(ImmutableList<TypeFilter> parameterTypes) {
			this.parameterTypes = parameterTypes;
		}

		@Override
		public boolean matches(Parameter[] parameters, boolean varArgs) {
			return varArgs ? matchesVarargs(parameters) : matchesNormal(parameters);

		}

		private boolean matchesVarargs(Parameter[] parameters) { // SUPPRESS UseVarargs
			checkArgument(parameters.length > 0);
			Parameter lastParameter = parameters[parameters.length - 1];
			Iterator<TypeFilter> expectedFilterIterator = parameterTypes.iterator();
			Iterator<Parameter> parameterIterator = Iterators.forArray(parameters);
			while (expectedFilterIterator.hasNext()) {
				TypeFilter typeFilter = expectedFilterIterator.next();
				Parameter parameter;
				if (parameterIterator.hasNext()) {
					parameter = parameterIterator.next();
				}
				else {
					parameter = lastParameter;
				}
				Class<?> actualParameterType = parameter.equals(lastParameter) ?
					parameter.getType().getComponentType() : parameter.getType();
				if (!typeFilter.matches(actualParameterType)) {
					return false;
				}
			}
			return !parameterIterator.hasNext()
				|| parameterIterator.next().equals(lastParameter);
		}

		private boolean matchesNormal(Parameter[] parameters) { // SUPPRESS UseVarargs
			Iterator<TypeFilter> expectedFilterIterator = parameterTypes.iterator();
			Iterator<Parameter> parameterIterator = Iterators.forArray(parameters);
			while (expectedFilterIterator.hasNext()) {
				if (!parameterIterator.hasNext()) {
					return false;
				}
				TypeFilter typeFilter = expectedFilterIterator.next();
				Parameter parameter = parameterIterator.next();
				Class<?> actualParameterType = parameter.getType();
				if (!typeFilter.matches(actualParameterType)) {
					return false;
				}
			}
			return !parameterIterator.hasNext();
		}
	}

	static final class Count implements ParametersFilter {
		static Count of(int number) {
			return new Count(number);
		}

		private final int number;

		private Count(int number) {
			this.number = number;
		}

		@Override
		public boolean matches(Parameter[] parameters, boolean varArgs) {
			return varArgs ? parameters.length <= number + 1 : parameters.length == number;
		}
	}

	private ParameterFilters() {
		// utility
	}
}
