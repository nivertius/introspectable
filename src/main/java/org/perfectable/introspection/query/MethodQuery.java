package org.perfectable.introspection.query;

import org.perfectable.introspection.PrivilegedActions;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * Iterable-like container that searches for methods in class.
 *
 * <p>This is straightforward way to search for methods that have specified characteristics in provided class.
 *
 * <p>Instances of this class are immutable, each filtering produces new, modified instance. To obtain unrestricted
 * query, use {@link #of}.
 *
 * <p>To obtain results either iterate this class with {@link #iterator} (or in enhanced-for loop) or use one of
 * {@link #stream()}, {@link #unique()}, {@link #option()} or {@link #isPresent()}.
 *
 * <p>All methods present in class are searched, either declared in it directly, or inherited from parent classes. This
 * means that even methods that are overridden in the inheritance chain will be returned, so methods with the same
 * signature but different declaring class might be present multiple times. To avoid this behavior, use
 * {@link #notOverridden}.
 *
 * <p>Example usage, which injects all public instance methods in class "UserService" that have annotation "Inject" on
 * them, have one parameter, and does not return value (is void). Before passing field to the method, it is marked
 * as {@link Method#setAccessible accessible}.
 * <pre>
 *     MethodQuery.of(UserService.class)
 *         .requiringModifier(Modifier.PUBLIC)
 *         .excludingModifier(Modifier.STATIC)
 *         .annotatedBy(Inject.class)
 *         .parameters(ParametersFilter.count(1))
 *         .returningVoid()
 *         .asAccessible()
 *         .stream()
 *         .forEach(this::inject);
 * </pre>
 */
@SuppressWarnings({
	"DesignForExtension", // class is closed because of package-private constructor
	"ClassDataAbstractionCoupling"
})
public abstract class MethodQuery extends ExecutableQuery<Method, MethodQuery> {

	/**
	 * Queries for methods in specified class.
	 *
	 * @param type class to search methods in
	 * @return query that returns all methods in specified class.
	 */
	public static MethodQuery of(Class<?> type) {
		return of(InheritanceQuery.of(type));
	}

	/**
	 * Queries for methods in classes contained in specified inheritance chain.
	 *
	 * @param type classes to search methods in
	 * @return query that returns all methods in specified classes.
	 */
	public static MethodQuery of(InheritanceQuery<?> type) {
		requireNonNull(type);
		return new InClasses<>(type);
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
	public MethodQuery sorted(Comparator<? super Method> comparator) {
		requireNonNull(comparator);
		return new Sorted(this, comparator);
	}

	@Override
	public MethodQuery parameters(ParametersFilter parametersFilter) {
		requireNonNull(parametersFilter);
		return new Parameters(this, parametersFilter);
	}

	/**
	 * Restricts query to methods that has a return type that is subtype of specified type.
	 *
	 * <p>This methods accepts every type, even {@link Void#TYPE}. In latter case, it will filter for methods that
	 * does not return a value, i.e. void methods. For readability, use {@link #returningVoid}.
	 *
	 * @param type return type to restrict
	 * @return copy of this query that will also filter by return type
	 */
	public MethodQuery returning(Type type) {
		return returning(TypeFilter.subtypeOf(type));
	}

	/**
	 * Restricts query to methods that has a return type that match the filter.
	 *
	 * @param typeFilter filter to restrict method return type with
	 * @return copy of this query that will also filter by return type
	 */
	public MethodQuery returning(TypeFilter typeFilter) {
		requireNonNull(typeFilter);
		return new Returning(this, typeFilter);
	}

	/**
	 * Restricts query to methods that doesn't return a value, i.e. are void.
	 *
	 * @apiNote This method could be named "notReturning", but its a matter of opinion if it would be more confusing
	 *     than current name. The void method actually returns, ends normally.
	 *
	 * @return copy of this query that will filter methods that have void return
	 */
	public MethodQuery returningVoid() {
		return returning(Void.TYPE);
	}

	/**
	 * Restricts query for methods that are not overridden by other in the inheritance chain.
	 *
	 * <p>Normally, the query returns methods that are overridden in the inheritance chain - methods with the same
	 * signature but different declaring class might be present multiple times. Using this method will change that
	 * behavior.
	 *
	 * @return copy of this query that will filter methods that are not overridden
	 */
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

	private static final class InClasses<X> extends MethodQuery {
		private final InheritanceQuery<X> chain;

		InClasses(InheritanceQuery<X> chain) {
			this.chain = chain;
		}

		@Override
		public Stream<Method> stream() {
			return this.chain.stream()
					.flatMap(testedClass -> Stream.of(testedClass.getDeclaredMethods()));
		}

		@Override
		public boolean contains(@Nullable Object candidate) {
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
		public boolean contains(@Nullable Object candidate) {
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

	private static final class Sorted extends MethodQuery {
		private final MethodQuery parent;
		private final Comparator<? super Method> comparator;

		Sorted(MethodQuery parent, Comparator<? super Method> comparator) {
			this.parent = parent;
			this.comparator = comparator;
		}

		@Override
		public MethodQuery sorted(Comparator<? super Method> nextComparator) {
			@SuppressWarnings("unchecked")
			Comparator<@Nullable Object> casted = (Comparator<@Nullable Object>) nextComparator;
			return new Sorted(parent, this.comparator.thenComparing(casted));
		}

		@Override
		public Stream<Method> stream() {
			return parent.stream().sorted(comparator);
		}

		@Override
		public boolean contains(@Nullable Object candidate) {
			return parent.contains(candidate);
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

	private static final class AccessibleMarking extends MethodQuery {
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
		public boolean contains(@Nullable Object candidate) {
			return parent.contains(candidate);
		}
	}

	private static final class NotOverriden extends MethodQuery {
		private final MethodQuery parent;

		NotOverriden(MethodQuery parent) {
			this.parent = parent;
		}

		@Override
		public Stream<Method> stream() {
			Set<Method> processedMethods = new HashSet<>();
			return parent.stream()
				.filter(candidate -> processedMethods.stream()
					.filter(processed -> hasEquivalentSignature(candidate, processed))
					.noneMatch(processed -> isOverriddenByAssumingSignature(candidate, processed)))
				.peek(processedMethods::add);
		}

		@Override
		public boolean contains(@Nullable Object candidate) {
			if (!(candidate instanceof Method)) {
				return false;
			}
			if (!parent.contains(candidate)) {
				return false;
			}
			Method candidateMethod = (Method) candidate;
			MethodQuery overriding = parent
				.parameters(ParametersFilter.typesExact(candidateMethod.getParameterTypes()))
				.named(candidateMethod.getName())
				.filter(method -> !candidate.equals(method))
				.filter(method -> isOverriddenByAssumingSignature(candidateMethod, method));
			return !overriding.isPresent();
		}

		private static boolean hasEquivalentSignature(Method left, Method right) {
			return left.getName().equals(right.getName())
				&& Arrays.equals(left.getParameterTypes(), right.getParameterTypes());
		}

		private static boolean isOverriddenByAssumingSignature(Method method, Method potentialOverride) {
			int modifiers = method.getModifiers();
			if (Modifier.isPrivate(modifiers)) {
				return false;
			}
			Class<?> declaringClass = method.getDeclaringClass();
			Class<?> potentialOverrideDeclaringClass = potentialOverride.getDeclaringClass();
			@Nullable Package methodPackage = declaringClass.getPackage();
			@Nullable Package potentialOverridePackage = potentialOverrideDeclaringClass.getPackage();
			boolean samePackage = Objects.equals(methodPackage, potentialOverridePackage);
			if (!Modifier.isProtected(modifiers) && !Modifier.isPublic(modifiers) && !samePackage) {
				return false;
			}
			return declaringClass.isAssignableFrom(potentialOverrideDeclaringClass);
		}
	}
}
