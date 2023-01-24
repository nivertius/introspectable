package org.perfectable.introspection.query;

import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.primitives.Primitives;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.ImmutableList.toImmutableList;

final class ParameterFilters {
	static ParametersFilter matchingArguments(@Nullable Object... arguments) {
		ImmutableList.Builder<TypeFilter> filters = ImmutableList.builder();
		for (@Nullable Object argument : arguments) {
			TypeFilter filter;
			if (argument == null) {
				filter = TypeFilter.PRIMITIVE.negated();
			}
			else {
				Class<?> argumentClass = argument.getClass();
				filter = TypeFilter.superTypeOf(argumentClass);
				if (Primitives.isWrapperType(argumentClass)) {
					filter = filter.or(TypeFilter.exact(Primitives.unwrap(argumentClass)));
				}
			}
			filters.add(filter);
		}
		return Types.of(filters.build());
	}

	static final class Types implements ParametersFilter {
		private final ImmutableList<TypeFilter> parameterTypes;

		static Types accepting(Type... parameterTypes) {
			ImmutableList<TypeFilter> typeFilters =
				Stream.of(parameterTypes).map(TypeFilter::superTypeOf).collect(toImmutableList());
			return new Types(typeFilters);
		}

		static Types exact(Type... parameterTypes) {
			ImmutableList<TypeFilter> typeFilters =
				Stream.of(parameterTypes).map(TypeFilter::exact).collect(toImmutableList());
			return of(typeFilters);
		}

		static Types of(TypeFilter... parameterTypes) {
			ImmutableList<TypeFilter> typeFilters = ImmutableList.copyOf(parameterTypes);
			return of(typeFilters);
		}

		static Types of(ImmutableList<TypeFilter> parameterTypes) {
			return new Types(parameterTypes);
		}

		private Types(ImmutableList<TypeFilter> parameterTypes) {
			this.parameterTypes = parameterTypes;
		}

		@Override
		public boolean matches(Parameter[] parameters, boolean varArgs) {
			return varArgs ? matchesVarargs(parameters) : matchesNormal(parameters);

		}

		private boolean matchesVarargs(Parameter[] parameters) {
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
				@SuppressWarnings("nullness:cast.unsafe")
				Class<?> actualParameterType = parameter.equals(lastParameter) ?
					(@NonNull Class<?>) parameter.getType().getComponentType() : parameter.getType();
				if (!typeFilter.matches(actualParameterType)) {
					return false;
				}
			}
			return !parameterIterator.hasNext()
				|| parameterIterator.next().equals(lastParameter);
		}

		private boolean matchesNormal(Parameter[] parameters) {
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
