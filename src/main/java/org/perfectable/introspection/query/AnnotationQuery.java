package org.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import static java.util.Objects.requireNonNull;

/**
 * Iterable-like container that finds annotations.
 *
 * <p>Instances of this class are immutable, each filtering produces new, modified instance. To obtain query for
 * specific class, use {@link #of}.
 *
 * <p>To obtain results either iterate this class with {@link #iterator} (or in enhanced-for loop) or use one of
 * {@link #stream()}, {@link #unique()}, {@link #option()} or {@link #isPresent()}.
 *
 * <p>This query handles repeatable annotations on elements as if they would be placed in a container annotation,
 * exactly as it is written in bytecode. To get behavior that was introduced in Java 1.8 in
 * {@link AnnotatedElement#getDeclaredAnnotationsByType}, this query needs manual expansion of container annotations.
 * See {@link #withRepeatableUnroll} for details.
 *
 * <p>Example usage, which collects all annotations on {@code singletonClass} that are meta-annotated by Qualifier.
 * <pre>
 *     AnnotationQuery.of(singletonClass)
 * 			.annotatedWith(javax.inject.Qualifier.class)
 * 			.stream()
 * 			.collect(ImmutableSet.toImmutableSet());
 * </pre>
 *
 * @param <A> base annotation type
 */
@SuppressWarnings({
	"DesignForExtension" // class is closed because of package-private constructor
})
public abstract class AnnotationQuery<A extends Annotation>
		extends AbstractQuery<A, AnnotationQuery<A>> {
	/**
	 * This simple Null Object Pattern for this query.
	 *
	 * @return Query which will produce no results.
	 */
	public static AnnotationQuery<Annotation> empty() {
		return Empty.INSTANCE;
	}

	/**
	 * Creates a query from annotations declared on specified element.
	 *
	 * <p>Only annotations with {@link java.lang.annotation.RetentionPolicy#RUNTIME} will be returned.
	 *
	 * @param element element to extract annotations from
	 * @return annotation query that will list annotations
	 */
	public static AnnotationQuery<Annotation> of(AnnotatedElement element) {
		return new OfElement(element);
	}

	/**
	 * Converts set of annotations to query.
	 *
	 * <p>Sets stream can be used to filter and does essentially the same, but this query allows more expressive filters
	 * used on specified set.
	 *
	 * @param set set to search on
	 * @return annotation query that will list exactly the set contents
	 */
	public static AnnotationQuery<Annotation> fromElements(Set<Annotation> set) {
		return new OfSet(ImmutableSet.copyOf(set));
	}

	/**
	 * Converts array of annotations to query.
	 *
	 * <p>Stream of array contents can be used to filter and does essentially the same, but this query allows more
	 * expressive filters used.
	 *
	 * @param elements annotations to search from to search on
	 * @return annotation query that will list exactly the array contents
	 */
	public static AnnotationQuery<Annotation> fromElements(Annotation... elements) {
		return fromElements(ImmutableSet.copyOf(elements));
	}

	/**
	 * Creates query that contains results from both this and provided query.
	 *
	 * @param other annotation query to add
	 * @return annotation query that will list results of this and then the other query
	 */
	@SuppressWarnings("unchecked")
	public AnnotationQuery<Annotation> join(AnnotationQuery<?> other) {
		return Composite.composite(this, other);
	}

	@Override
	public AnnotationQuery<A> filter(Predicate<? super A> filter) {
		requireNonNull(filter);
		return new Predicated<>(this, filter);
	}

	/**
	 * Creates query that filters resulting annotations that have meta-annotation placed on them.
	 *
	 * @param metaAnnotation annotation that must be placed on resulting annotation to be returned by query
	 * @return annotation query that will list results that have specific annotations placed on them
	 */
	public AnnotationQuery<A> annotatedWith(Class<? extends Annotation> metaAnnotation) {
		requireNonNull(metaAnnotation);
		return new Annotated<>(this, metaAnnotation);
	}

	/**.
	 * Creates query that filters annotation that have specific type as a supertype.
	 *
	 * @param annotationClass annotation type to filters
	 * @param <X> new query result type
	 * @return annotation query that will list results that are of specific type
	 */
	public <X extends Annotation> AnnotationQuery<X> typed(Class<X> annotationClass) {
		requireNonNull(annotationClass);
		return new Typed<>(this, annotationClass);
	}

	/**
	 * Expands repeatable annotations for the results of returned query.
	 *
	 * <p>Normally, query handles repeatable annotations on elements as if they would be placed in a container
	 * annotation, exactly as it is written in bytecode. This method will add that repeatable annotations to the
	 * container.
	 *
	 * <p>This method changes results, so that every annotation, that would be returned, if its a
	 * containing annotation, annotations that are embedded in it are also returned as a results. Containing
	 * annotation is also returned, if it matches other filters. See JLS 9.6.3.
	 *
	 * <p>Unfortunately, repeatable annotation extraction in native Java is done with special care that is unavailable
	 * for public, for example in OpenJDK, the sun.reflect.annotation.AnnotationType is used, which is contained in a
	 * Class instance of an annotation type. Both field containing and the type is not public. Therefore this
	 * query tries to reconstruct relationship between containing and contained types. This has some initial performance
	 * penalty, as "value" method must be reflected. This class caches the information, so successive calls should
	 * be faster.	 *
	 *
	 * @return annotation query that will list repeatable annotations extracted from containers
	 */
	public AnnotationQuery<Annotation> withRepeatableUnroll() {
		return new RepeatableUnroll(this);
	}

	private static final class OfElement extends AnnotationQuery<Annotation> {
		private final AnnotatedElement element;

		OfElement(AnnotatedElement element) {
			this.element = element;
		}

		@Override
		public Stream<Annotation> stream() {
			return Stream.of(element.getAnnotations());
		}
	}

	private static final class OfSet extends AnnotationQuery<Annotation> {
		private final ImmutableSet<Annotation> elements;

		OfSet(ImmutableSet<Annotation> elements) {
			this.elements = elements;
		}

		@Override
		public Stream<Annotation> stream() {
			return elements.stream();
		}
	}

	private abstract static class Filtered<A extends Annotation> extends AnnotationQuery<A> {
		private final AnnotationQuery<A> parent;

		Filtered(AnnotationQuery<A> parent) {
			this.parent = parent;
		}

		protected abstract boolean matches(A candidate);

		@Override
		public Stream<A> stream() {
			return this.parent.stream()
					.filter(this::matches);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(Object candidate) {
			if (!(candidate instanceof Annotation)) {
				return false;
			}
			return matches((A) candidate) && parent.contains(candidate);
		}
	}

	private static final class Predicated<A extends Annotation> extends Filtered<A> {
		private final Predicate<? super A> filter;

		Predicated(AnnotationQuery<A> parent, Predicate<? super A> filter) {
			super(parent);
			this.filter = filter;
		}

		@Override
		protected boolean matches(A candidate) {
			return filter.test(candidate);
		}
	}

	private static final class Annotated<A extends Annotation> extends Filtered<A> {
		private final Class<? extends Annotation> metaAnnotation;

		Annotated(AnnotationQuery<A> parent, Class<? extends Annotation> metaAnnotation) {
			super(parent);
			this.metaAnnotation = metaAnnotation;
		}

		@Override
		protected boolean matches(A candidate) {
			return candidate.annotationType().isAnnotationPresent(metaAnnotation);
		}
	}

	private static final class Typed<X extends Annotation> extends AnnotationQuery<X> {
		private final AnnotationQuery<?> parent;
		private final Class<X> type;

		Typed(AnnotationQuery<?> parent, Class<X> type) {
			this.parent = parent;
			this.type = type;
		}

		@Override
		public Stream<X> stream() {
			return parent.stream()
					.filter(type::isInstance)
					.map(type::cast);
		}

		@Override
		public boolean contains(Object candidate) {
			return type.isInstance(candidate) && parent.contains(candidate);
		}
	}

	private static final class Empty extends AnnotationQuery<Annotation> {
		private static final Empty INSTANCE = new Empty();

		@Override
		public Stream<Annotation> stream() {
			return Stream.empty();
		}

		@Override
		public Empty filter(Predicate<? super Annotation> filter) {
			return Empty.INSTANCE;
		}

		@SuppressWarnings("unchecked")
		@Override
		public AnnotationQuery<Annotation> join(AnnotationQuery<? extends Annotation> other) {
			return (AnnotationQuery<Annotation>) other;
		}

		@Override
		public boolean contains(Object candidate) {
			return false;
		}
	}

	private static final class RepeatableUnroll extends AnnotationQuery<Annotation> {
		private static final Set<Class<? extends Annotation>> KNOWN_NON_CONTAINERS =
			Collections.newSetFromMap(new ConcurrentHashMap<>());
		private static final Map<Class<? extends Annotation>, Method> KNOWN_CONTAINERS =
			new ConcurrentHashMap<>();

		private final AnnotationQuery<?> parent;

		RepeatableUnroll(AnnotationQuery<?> parent) {
			this.parent = parent;
		}

		@Override
		public Stream<Annotation> stream() {
			return parent.stream()
				.flatMap(this::unroll);
		}

		@Override
		public boolean contains(Object candidate) {
			if (parent.contains(candidate)) {
				return true;
			}
			if (!(candidate instanceof Annotation)) {
				return false;
			}
			Annotation candidateAnnotation = (Annotation) candidate;
			Repeatable repeatableAnnotation =
				candidateAnnotation.annotationType().getAnnotation(Repeatable.class);
			if (repeatableAnnotation == null) {
				return false;
			}
			Class<? extends Annotation> containerType = repeatableAnnotation.value();
			Method extractionMethod = KNOWN_CONTAINERS.computeIfAbsent(containerType,
				RepeatableUnroll::findContainerMethod);
			return parent.typed(containerType).stream()
				.flatMap(container -> extractContents(container, extractionMethod))
				.anyMatch(candidate::equals);
		}

		private Stream<Annotation> unroll(Annotation source) {
			Stream<Annotation> baseResult = Stream.of(source);
			Class<? extends Annotation> sourceClass = source.annotationType();
			if (KNOWN_NON_CONTAINERS.contains(sourceClass)) {
				return baseResult;
			}
			try {
				Method extractionMethod = KNOWN_CONTAINERS.computeIfAbsent(sourceClass,
					RepeatableUnroll::findContainerMethod);
				Stream<Annotation> additionalResults = extractContents(source, extractionMethod);
				return Stream.concat(baseResult, additionalResults);
			}
			catch (IllegalArgumentException e) {
				KNOWN_NON_CONTAINERS.add(sourceClass);
				return baseResult;
			}
		}

		private static Method findContainerMethod(Class<? extends Annotation> candidateContainer)
				throws IllegalArgumentException {
			Method[] methods = candidateContainer.getDeclaredMethods();
			Optional<Method> valueMethodOption =
				Stream.of(methods).filter(method -> method.getName().equals("value")).findAny();
			if (!valueMethodOption.isPresent()) {
				throw new IllegalArgumentException();
			}
			Method valueMethod = valueMethodOption.get();
			Class<?> returnType = valueMethod.getReturnType();
			if (!returnType.isArray()
				|| !Annotation.class.isAssignableFrom(returnType.getComponentType())) {
				throw new IllegalArgumentException();
			}
			@SuppressWarnings("unchecked")
			Class<? extends Annotation> containedClass =
				(Class<? extends Annotation>) returnType.getComponentType();
			Repeatable repeatableAnnotation = containedClass.getAnnotation(Repeatable.class);
			if (!repeatableAnnotation.value().equals(candidateContainer)) {
				throw new IllegalArgumentException();
			}
			return valueMethod;
		}

		public Stream<Annotation> extractContents(Annotation source, Method extractionMethod) {
			Object resultArray;
			try {
				resultArray = extractionMethod.invoke(source);
			}
			catch (IllegalAccessException | InvocationTargetException e) {
				throw new AssertionError(e);
			}
			return IntStream.range(0, Array.getLength(resultArray))
				.mapToObj(i -> (Annotation) Array.get(resultArray, i));
		}
	}

	private static final class Composite<A extends Annotation> extends AnnotationQuery<A> {
		private final ImmutableList<AnnotationQuery<? extends A>> components;

		private Composite(ImmutableList<AnnotationQuery<? extends A>> components) {
			this.components = components;
		}

		@SuppressWarnings("unchecked")
		private static <A extends Annotation> AnnotationQuery<A> composite(AnnotationQuery<? extends A>... components) {
			return new Composite<>(ImmutableList.copyOf(components));
		}

		@Override
		public Stream<A> stream() {
			return components.stream().flatMap(AbstractQuery::stream);
		}

		@Override
		public AnnotationQuery<Annotation> join(AnnotationQuery<?> other) {
			ImmutableList<AnnotationQuery<?>> newComponents =
					ImmutableList.<AnnotationQuery<?>>builder()
						.addAll(components).add(other).build();
			return new Composite<Annotation>(newComponents);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(Object candidate) {
			return components.stream().anyMatch(component -> component.contains(candidate));
		}
	}

	AnnotationQuery() {
		// package extension only
	}
}
