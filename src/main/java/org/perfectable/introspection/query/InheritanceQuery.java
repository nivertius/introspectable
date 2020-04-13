package org.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Iterable-like container that allows access to supertypes of a class.
 *
 * <p>This of course only lists types that are actually defined (transitively) by initially-provided class. Altrough
 * wildcard types, type variables might be supertype or supertype of specified type, they are not listed.
 *
 * <p>Instances of this class are immutable, each filtering produces new, modified instance. To obtain query for
 * specific class, use {@link #of}.
 *
 * <p>To obtain results either iterate this class with {@link #iterator} (or in enhanced-for loop) or use one of
 * {@link #stream()}, {@link #unique()}, {@link #option()} or {@link #isPresent()}.
 *
 * <p>Example usage, which registers all interfaces that DatabaseService implements that are annotated by Remote
 * <pre>
 *     InheritanceQuery.of(DatabaseService.class)
 *         .annotatedWith(Remote.class)
 *         .onlyInterfaces()
 *         .stream()
 *         .forEach(this::register);
 * </pre>
 *
 * @param <X> lower bound of the searched types
 */
@SuppressWarnings({
	"DesignForExtension" // class is closed because of package-private constructor
})
public abstract class InheritanceQuery<X> extends AbstractQuery<Class<? super X>, InheritanceQuery<X>> {
	/**
	 * Creates unrestricted query that will list all the supertypes that this class or interface extends/implements.
	 *
	 * @param type class to start search with
	 * @param <X> upper subtype of all results
	 * @return new, unrestricted inheritance query
	 */
	public static <X> InheritanceQuery<X> of(Class<X> type) {
		return new Complete<>(type);
	}

	/**
	 * Creates query which lists the same classes as this one, but only if they have an annotation with provided class.
	 *
	 * @param annotationClass annotation class that will be used
	 * @return query filtered for classes annotated with specified class
	 */
	public InheritanceQuery<X> annotatedWith(Class<? extends Annotation> annotationClass) {
		return annotatedWith(AnnotationFilter.single(annotationClass));
	}

	/**
	 * Creates query which lists the same classes as this one, but only if they have an annotation that matches
	 * specified filter.
	 *
	 * @param annotationFilter annotation filter that will be used
	 * @return query filtered for classes annotated with specific properties
	 */
	public InheritanceQuery<X> annotatedWith(AnnotationFilter annotationFilter) {
		requireNonNull(annotationFilter);
		return new Annotated<>(this, annotationFilter);
	}

	@Override
	public InheritanceQuery<X> filter(Predicate<? super Class<? super X>> filter) {
		return new Predicated<>(this, filter);
	}

	/**
	 * Create query which lists the same classes as this one, but only if they are subtypes of provided type, and are
	 * not this type.
	 *
	 * @param supertype supertype of the filtered classes
	 * @return query filtered for subtypes of specified type, excluding it
	 */
	public InheritanceQuery<X> upToExcluding(Class<? super X> supertype) {
		return new BoundingExcluded<>(this, supertype);
	}

	/**
	 * Create query which lists the same classes as this one, but only if they are subtypes of provided type.
	 *
	 * <p>Note that each type is its own subtype, so if {@code supertype} is actually supertype of initial class
	 * or interface, it will be included in results.
	 *
	 * @param supertype supertype of the filtered classes
	 * @return query filtered for subtypes of specified type, including it
	 */
	public InheritanceQuery<X> upToIncluding(Class<? super X> supertype) {
		return new BoundingIncluded<>(this, supertype);
	}

	/**
	 * Creates query which lists the same classes as this one, but only if they are actually an interface.
	 *
	 * @return query filtered for interfaces
	 */
	public InheritanceQuery<X> onlyInterfaces() {
		return new InterfacesOnly<>(this);
	}

	/**
	 * Creates query which lists the same classes as this one, but only if they are actually a class (not an interface).
	 *
	 * @return query filtered for classes
	 */
	public InheritanceQuery<X> onlyClasses() {
		return new ClassesOnly<>(this);
	}

	InheritanceQuery() {
		// package extension only
	}

	private static final class Complete<X> extends InheritanceQuery<X> {

		private final Class<X> initial;

		Complete(Class<X> initial) {
			this.initial = initial;
		}

		@Override
		public Stream<Class<? super X>> stream() {
			return Streams.generateSingle(initial, InheritanceQuery::safeGetSupertypes);
		}

		@Override
		public boolean contains(Object candidate) {
			if (!(candidate instanceof Class<?>)) {
				return false;
			}
			@SuppressWarnings("unchecked")
			Class<? super X> candidateClass = (Class<? super X>) candidate;
			return candidateClass.isAssignableFrom(initial);
		}
	}

	private abstract static class Filtered<X> extends InheritanceQuery<X> {

		private final InheritanceQuery<X> parent;

		Filtered(InheritanceQuery<X> parent) {
			this.parent = parent;
		}

		protected abstract boolean matches(Class<? super X> candidate);

		@Override
		public Stream<Class<? super X>> stream() {
			return this.parent.stream()
					.filter(this::matches);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(Object candidate) {
			if (!(candidate instanceof Class<?>)) {
				return false;
			}
			@SuppressWarnings("unchecked")
			Class<? super X> candidateClass = (Class<? super X>) candidate;
			return matches(candidateClass) && parent.contains(candidate);
		}
	}

	private static final class Annotated<X> extends Filtered<X> {
		private final AnnotationFilter annotationFilter;

		Annotated(InheritanceQuery<X> parent, AnnotationFilter annotationFilter) {
			super(parent);
			this.annotationFilter = annotationFilter;
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return this.annotationFilter.matches(candidate);
		}
	}

	private static final class Predicated<X> extends Filtered<X> {

		private final Predicate<? super Class<? super X>> filter;

		Predicated(InheritanceQuery<X> parent, Predicate<? super Class<? super X>> filter) {
			super(parent);
			this.filter = filter;
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return filter.test(candidate);
		}
	}

	private static final class BoundingExcluded<X> extends Filtered<X> {

		private final Class<? super X> supertype;

		BoundingExcluded(InheritanceQuery<X> parent, Class<? super X> supertype) {
			super(parent);
			this.supertype = supertype;
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return supertype.isAssignableFrom(candidate) && !supertype.equals(candidate);
		}
	}

	private static final class BoundingIncluded<X> extends Filtered<X> {

		private final Class<? super X> supertype;

		BoundingIncluded(InheritanceQuery<X> parent, Class<? super X> supertype) {
			super(parent);
			this.supertype = supertype;
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return supertype.isAssignableFrom(candidate);
		}
	}

	private static final class InterfacesOnly<X> extends Filtered<X> {

		InterfacesOnly(InheritanceQuery<X> parent) {
			super(parent);
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return candidate.isInterface();
		}
	}

	private static final class ClassesOnly<X> extends Filtered<X> {

		ClassesOnly(InheritanceQuery<X> parent) {
			super(parent);
		}

		@Override
		protected boolean matches(Class<? super X> candidate) {
			return !candidate.isInterface();
		}
	}

	private static <X> Stream<Class<? super X>> safeGetSupertypes(Class<? super X> type) {
		Stream.Builder<Class<? super X>> builder = Stream.builder();
		@SuppressWarnings("unchecked")
		Class<? super X>[] interfaceArray = (Class<? super X>[]) type.getInterfaces();
		Stream.of(interfaceArray).forEach(builder);
		Class<? super X> superclass = type.getSuperclass();
		if (superclass != null) {
			builder.accept(superclass);
		}
		return builder.build();
	}
}
