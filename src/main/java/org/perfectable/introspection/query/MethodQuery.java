package org.perfectable.introspection.query;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

// SUPPRESS NEXT 1 ClassDataAbstractionCoupling
public abstract class MethodQuery extends ExecutableQuery<Method, MethodQuery> {

	public static <X> MethodQuery of(Class<X> type) {
		requireNonNull(type);
		return new Complete<>(type);
	}

	@Override
	public MethodQuery named(String name) {
		requireNonNull(name);
		return new Named(this, name);
	}

	@Override
	public MethodQuery nameMatching(Pattern namePattern) {
		requireNonNull(namePattern);
		return new NameMatching(this, namePattern);
	}

	@Override
	public MethodQuery filter(Predicate<? super Method> filter) {
		requireNonNull(filter);
		return new Predicated(this, filter);
	}

	@Override
	public MethodQuery parameters(ParametersFilter parametersFilter) {
		requireNonNull(parametersFilter);
		return new Parameters(this, parametersFilter);
	}

	// only implements super
	@Deprecated
	@Override
	public MethodQuery typed(Class<?> type) {
		return returning(type);
	}

	public MethodQuery returning(Class<?> type) {
		requireNonNull(type);
		return new Returning(this, type);
	}

	public MethodQuery returningVoid() {
		return returning(Void.TYPE);
	}

	@Override
	public MethodQuery annotatedWith(AnnotationFilter annotationFilter) {
		requireNonNull(annotationFilter);
		return new Annotated(this, annotationFilter);
	}

	@Override
	public MethodQuery excludingModifier(int excludedModifier) {
		return new ExcludingModifier(this, excludedModifier);
	}

	@Override
	public MethodQuery asAccessible() {
		return new AccessibleMarking(this);
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

	private static final class NameMatching extends Filtered {
		private final Pattern namePattern;

		NameMatching(MethodQuery parent, Pattern namePattern) {
			super(parent);
			this.namePattern = namePattern;
		}

		@Override
		protected boolean matches(Method candidate) {
			return this.namePattern.matcher(candidate.getName()).matches();
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

	private static class AccessibleMarking extends MethodQuery {
		private final MethodQuery parent;

		AccessibleMarking(MethodQuery parent) {
			this.parent = parent;
		}

		@Override
		public Stream<Method> stream() {
			return parent.stream()
				.peek(field -> field.setAccessible(true));
		}
	}
}
