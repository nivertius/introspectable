package org.perfectable.introspection.query;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

// SUPPRESS NEXT 1 ClassDataAbstractionCoupling
public abstract class ConstructorQuery<X> extends ExecutableQuery<Constructor<X>, ConstructorQuery<X>> {

	public static <X> ConstructorQuery<X> of(Class<X> type) {
		requireNonNull(type);
		return new Complete<>(type);
	}

	@Override
	public ConstructorQuery<X> named(String name) {
		requireNonNull(name);
		return new Named<>(this, name);
	}

	@Override
	public ConstructorQuery<X> nameMatching(Pattern namePattern) {
		requireNonNull(namePattern);
		return new NameMatching<>(this, namePattern);
	}

	@Override
	public ConstructorQuery<X> filter(Predicate<? super Constructor<X>> filter) {
		requireNonNull(filter);
		return new Predicated<>(this, filter);
	}

	@Override
	public ConstructorQuery<X> parameters(ParametersFilter parametersFilter) {
		requireNonNull(parametersFilter);
		return new Parameters<>(this, parametersFilter);
	}

	@Override
	public ConstructorQuery<X> annotatedWith(AnnotationFilter annotationFilter) {
		requireNonNull(annotationFilter);
		return new Annotated<>(this, annotationFilter);
	}

	@Override
	public ConstructorQuery<X> requiringModifier(int requiredModifier) {
		return new RequiringModifier<>(this, requiredModifier);
	}

	@Override
	public ConstructorQuery<X> excludingModifier(int excludedModifier) {
		return new ExcludingModifier<>(this, excludedModifier);
	}

	@Override
	public ConstructorQuery<X> asAccessible() {
		return new AccessibleMarking<>(this);
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


	private static final class NameMatching<X> extends Filtered<X> {
		private final Pattern namePattern;

		NameMatching(ConstructorQuery<X> parent, Pattern namePattern) {
			super(parent);
			this.namePattern = namePattern;
		}

		@Override
		protected boolean matches(Constructor<X> candidate) {
			return this.namePattern.matcher(candidate.getName()).matches();
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
			return parametersFilter.matches(candidate);
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

	private static final class RequiringModifier<X> extends Filtered<X> {
		private final int requiredModifier;

		RequiringModifier(ConstructorQuery<X> parent, int requiredModifier) {
			super(parent);
			this.requiredModifier = requiredModifier;
		}

		@Override
		protected boolean matches(Constructor<X> candidate) {
			return (candidate.getModifiers() & this.requiredModifier) != 0;
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

	private static class AccessibleMarking<X> extends ConstructorQuery<X> {
		private final ConstructorQuery<X> parent;

		AccessibleMarking(ConstructorQuery<X> parent) {
			this.parent = parent;
		}

		@Override
		public Stream<Constructor<X>> stream() {
			return parent.stream()
				.peek(field -> field.setAccessible(true));
		}
	}
}
