package org.perfectable.introspection.query; // SUPPRESS LENGTH

import org.perfectable.introspection.PrivilegedActions;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.common.base.Equivalence;

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

	public MethodQuery returning(Class<?> type) {
		return returning(TypeFilter.subtypeOf(type));
	}

	public MethodQuery returning(TypeFilter typeFilter) {
		requireNonNull(typeFilter);
		return new Returning(this, typeFilter);
	}

	public MethodQuery returningVoid() {
		return returning(Void.TYPE);
	}

	public MethodQuery notOverridden() {
		return new NotOverriden(this);
	}

	@Override
	public MethodQuery annotatedWith(AnnotationFilter annotationFilter) {
		requireNonNull(annotationFilter);
		return new Annotated(this, annotationFilter);
	}

	@Override
	public MethodQuery requiringModifier(int requiredModifier) {
		return new RequiringModifier(this, requiredModifier);
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

		@Override
		public boolean contains(Object candidate) {
			if (!(candidate instanceof Method)) {
				return false;
			}
			Method candidateMethod = (Method) candidate;
			@SuppressWarnings("unchecked")
			Class<? super X> declaringClass = (Class<? super X>) candidateMethod.getDeclaringClass();
			return chain.contains(declaringClass);
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

		@Override
		public boolean contains(Object candidate) {
			if (!(candidate instanceof Method)) {
				return false;
			}
			Method candidateMethod = (Method) candidate;
			return matches(candidateMethod) && parent.contains(candidate);
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
			return parametersFilter.matches(candidate);
		}
	}

	private static final class Returning extends Filtered {
		private final TypeFilter typeFilter;

		Returning(MethodQuery parent, TypeFilter typeFilter) {
			super(parent);
			this.typeFilter = typeFilter;
		}

		@Override
		protected boolean matches(Method candidate) {
			return this.typeFilter.matches(candidate.getReturnType());
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

	private static final class RequiringModifier extends Filtered {
		private final int requiredModifier;

		RequiringModifier(MethodQuery parent, int requiredModifier) {
			super(parent);
			this.requiredModifier = requiredModifier;
		}

		@Override
		protected boolean matches(Method candidate) {
			return (candidate.getModifiers() & this.requiredModifier) != 0;
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
				.peek(PrivilegedActions::markAccessible);
		}

		@Override
		public boolean contains(Object candidate) {
			return parent.contains(candidate);
		}
	}

	private static final class NotOverriden extends MethodQuery {
		static final Equivalence<Method> SIGNATURE_EQUIVALENCE = new Equivalence<Method>() {
			@Override
			protected boolean doEquivalent(Method left, Method right) {
				return left.getName().equals(right.getName())
					&& Arrays.equals(left.getParameterTypes(), right.getParameterTypes());
			}

			@Override
			protected int doHash(Method method) {
				return Objects.hash(method.getName(), Arrays.hashCode(method.getParameterTypes()));
			}
		};

		private final MethodQuery parent;

		NotOverriden(MethodQuery parent) {
			this.parent = parent;
		}

		@Override
		public Stream<Method> stream() {
			Set<Equivalence.Wrapper<Method>> processed = new HashSet<>();
			return parent.stream()
				.map(SIGNATURE_EQUIVALENCE::wrap)
				.filter(signature -> !processed.contains(signature))
				.peek(processed::add)
				.map(Equivalence.Wrapper::get);
		}

		@Override
		public boolean contains(Object candidate) {
			if (!(candidate instanceof Method)) {
				return false;
			}
			if (!parent.contains(candidate)) {
				return false;
			}
			Method candidateMethod = (Method) candidate;
			Class<?> declaringClass = candidateMethod.getDeclaringClass();
			MethodQuery sameSignature = parent
				.parameters(ParametersFilter.typesExact(candidateMethod.getParameterTypes()))
				.named(candidateMethod.getName());
			return sameSignature.stream()
					.map(Method::getDeclaringClass)
					.allMatch(methodClass -> methodClass.isAssignableFrom(declaringClass));
		}
	}
}
