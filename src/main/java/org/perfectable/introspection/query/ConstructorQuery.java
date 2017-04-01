package org.perfectable.introspection.query;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class ConstructorQuery<X> extends ExecutableQuery<Constructor<X>, ConstructorQuery<X>> {

	public static <X> ConstructorQuery<X> of(Class<X> type) {
		checkNotNull(type);
		return new Complete<>(type);
	}

	@Override
	public ConstructorQuery<X> named(String name) {
		checkNotNull(name);
		return new Named<>(this, name);
	}

	@Override
	public ConstructorQuery<X> filter(Predicate<? super Constructor<X>> filter) {
		checkNotNull(filter);
		return new Predicated<>(this, filter);
	}

	@Override
	public ConstructorQuery<X> parameters(ParametersFilter parametersFilter) {
		checkNotNull(parametersFilter);
		return new Parameters<>(this, parametersFilter);
	}

	@Override
	public ConstructorQuery<X> typed(Class<?> type) {
		checkNotNull(type);
		return new Typed<>(this, type);
	}

	@Override
	public ConstructorQuery<X> annotatedWith(AnnotationFilter annotationFilter) {
		checkNotNull(annotationFilter);
		return new Annotated<>(this, annotationFilter);
	}

	@Override
	public ConstructorQuery<X> excludingModifier(int excludedModifier) {
		return new ExcludingModifier<>(this, excludedModifier);
	}

	private static class Complete<X> extends ConstructorQuery<X> {
		private final Class<X> type;

		Complete(Class<X> type) {
			this.type = type;
		}

		@Override
		public Stream<Constructor<X>> stream() {
			@SuppressWarnings("unchecked")
			Constructor<X>[] declaredConstructors = (Constructor<X>[]) type.getDeclaredConstructors();
			return Arrays.stream(declaredConstructors);
		}
	}


	private abstract static class Filtered<X> extends ConstructorQuery<X> {
		private final ConstructorQuery<X> parent;

		Filtered(ConstructorQuery<X> parent) {
			this.parent = parent;
		}

		protected abstract boolean matches(Constructor<X> candidate);

		@Override
		public Stream<Constructor<X>> stream() {
			return this.parent.stream()
					.filter(this::matches);
		}
	}

	private static final class Predicated<X> extends Filtered<X> {
		private final Predicate<? super Constructor<X>> filter;

		Predicated(ConstructorQuery<X> parent, Predicate<? super Constructor<X>> filter) {
			super(parent);
			this.filter = filter;
		}

		@Override
		protected boolean matches(Constructor<X> candidate) {
			return this.filter.test(candidate);
		}
	}

	private static final class Named<X> extends Filtered<X> {
		private final String name;

		Named(ConstructorQuery<X> parent, String name) {
			super(parent);
			this.name = name;
		}

		@Override
		protected boolean matches(Constructor<X> candidate) {
			return this.name.equals(candidate.getName());
		}
	}

	private static final class Parameters<X> extends Filtered<X> {
		private final ParametersFilter parametersFilter;

		Parameters(ConstructorQuery<X> parent, ParametersFilter parametersFilter) {
			super(parent);
			this.parametersFilter = parametersFilter;
		}

		@Override
		protected boolean matches(Constructor<X> candidate) {
			Parameter[] parameters = candidate.getParameters();
			return parametersFilter.matches(parameters);
		}
	}

	private static final class Typed<X> extends Filtered<X> {
		private final Class<?> type;

		Typed(ConstructorQuery<X> parent, Class<?> type) {
			super(parent);
			this.type = type;
		}

		@Override
		protected boolean matches(Constructor<X> candidate) {
			return this.type.equals(candidate.getDeclaringClass());
		}
	}

	private static final class Annotated<X> extends Filtered<X> {
		private final AnnotationFilter annotationFilter;

		Annotated(ConstructorQuery<X> parent, AnnotationFilter annotationFilter) {
			super(parent);
			this.annotationFilter = annotationFilter;
		}

		@Override
		protected boolean matches(Constructor<X> candidate) {
			return this.annotationFilter.matches(candidate);
		}
	}

	private static final class ExcludingModifier<X> extends Filtered<X> {
		private final int excludedModifier;

		ExcludingModifier(ConstructorQuery<X> parent, int excludedModifier) {
			super(parent);
			this.excludedModifier = excludedModifier;
		}

		@Override
		protected boolean matches(Constructor<X> candidate) {
			return (candidate.getModifiers() & this.excludedModifier) == 0;
		}
	}
}
