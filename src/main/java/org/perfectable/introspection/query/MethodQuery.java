package org.perfectable.introspection.query;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class MethodQuery extends MemberQuery<Method, MethodQuery> {

	public static <X> MethodQuery of(Class<X> type) {
		checkNotNull(type);
		return new Complete<>(type);
	}

	@Override
	public MethodQuery named(String name) {
		checkNotNull(name);
		return new Named(this, name);
	}

	@Override
	public MethodQuery filter(Predicate<? super Method> filter) {
		checkNotNull(filter);
		return new Predicated(this, filter);
	}

	public MethodQuery parameters(ParametersFilter parametersFilter) {
		checkNotNull(parametersFilter);
		return new Parameters(this, parametersFilter);
	}

	public MethodQuery parameters(Class<?>... parameterTypes) {
		checkNotNull(parameterTypes);
		return parameters(ParametersFilter.types(parameterTypes));
	}

	// only implements super
	@Deprecated
	@Override
	public MethodQuery typed(Class<?> type) {
		return returning(type);
	}

	public MethodQuery returning(Class<?> type) {
		checkNotNull(type);
		return new Returning(this, type);
	}

	public MethodQuery returningVoid() {
		return returning(Void.TYPE);
	}

	@Override
	public MethodQuery annotatedWith(AnnotationFilter annotationFilter) {
		checkNotNull(annotationFilter);
		return new Annotated(this, annotationFilter);
	}

	@Override
	public MethodQuery excludingModifier(int excludedModifier) {
		return new ExcludingModifier(this, excludedModifier);
	}

	MethodQuery() {
		// package extension only
	}

	private static final class Complete<X> extends MethodQuery {
		private final InheritanceQuery<X> chain;

		Complete(Class<X> type) {
			this.chain = InheritanceQuery.of(type);
		}

		@Override
		public Stream<Method> stream() {
			return this.chain.stream()
					.flatMap(testedClass -> Stream.of(testedClass.getDeclaredMethods()));
		}
	}

	private abstract static class Filtered extends MethodQuery {
		private final MethodQuery parent;

		Filtered(MethodQuery parent) {
			this.parent = parent;
		}

		protected abstract boolean matches(Method candidate);

		@Override
		public Stream<Method> stream() {
			return this.parent.stream()
					.filter(this::matches);
		}
	}

	private static final class Predicated extends Filtered {
		private final Predicate<? super Method> filter;

		Predicated(MethodQuery parent, Predicate<? super Method> filter) {
			super(parent);
			this.filter = filter;
		}

		@Override
		protected boolean matches(Method candidate) {
			return this.filter.test(candidate);
		}
	}

	private static final class Named extends Filtered {
		private final String name;

		Named(MethodQuery parent, String name) {
			super(parent);
			this.name = name;
		}

		@Override
		protected boolean matches(Method candidate) {
			return this.name.equals(candidate.getName());
		}
	}

	private static final class Parameters extends Filtered {
		private final ParametersFilter parametersFilter;

		Parameters(MethodQuery parent, ParametersFilter parametersFilter) {
			super(parent);
			this.parametersFilter = parametersFilter;
		}

		@Override
		protected boolean matches(Method candidate) {
			Parameter[] parameters = candidate.getParameters();
			return parametersFilter.matches(parameters);
		}
	}

	private static final class Returning extends Filtered {
		private final Class<?> returnType;

		Returning(MethodQuery parent, Class<?> returnType) {
			super(parent);
			this.returnType = returnType;
		}

		@Override
		protected boolean matches(Method candidate) {
			return this.returnType.equals(candidate.getReturnType());
		}
	}

	private static final class Annotated extends Filtered {
		private final AnnotationFilter annotationFilter;

		Annotated(MethodQuery parent, AnnotationFilter annotationFilter) {
			super(parent);
			this.annotationFilter = annotationFilter;
		}

		@Override
		protected boolean matches(Method candidate) {
			return this.annotationFilter.matches(candidate);
		}
	}

	private static final class ExcludingModifier extends Filtered {
		private final int excludedModifier;

		ExcludingModifier(MethodQuery parent, int excludedModifier) {
			super(parent);
			this.excludedModifier = excludedModifier;
		}

		@Override
		protected boolean matches(Method candidate) {
			return (candidate.getModifiers() & this.excludedModifier) == 0;
		}
	}

}
