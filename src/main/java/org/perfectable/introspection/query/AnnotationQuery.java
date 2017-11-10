package org.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import static java.util.Objects.requireNonNull;

public abstract class AnnotationQuery<A extends Annotation>
		extends AbstractQuery<A, AnnotationQuery<A>> {

	public static AnnotationQuery<Annotation> empty() {
		return Empty.INSTANCE;
	}

	public static AnnotationQuery<Annotation> of(AnnotatedElement element) {
		return new OfElement(element);
	}

	@SuppressWarnings("unchecked")
	public AnnotationQuery<Annotation> join(AnnotationQuery<?> other) {
		return Composite.composite(this, other);
	}

	@Override
	public AnnotationQuery<A> filter(Predicate<? super A> filter) {
		requireNonNull(filter);
		return new Predicated<>(this, filter);
	}

	public AnnotationQuery<A> annotatedWith(Class<? extends Annotation> metaAnnotation) {
		requireNonNull(metaAnnotation);
		return new Annotated<>(this, metaAnnotation);
	}

	public <X extends A> AnnotationQuery<X> typed(Class<X> annotationClass) {
		requireNonNull(annotationClass);
		return new Typed<>(this, annotationClass);
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

	private abstract static class Filtered<A extends Annotation>
			extends AnnotationQuery<A> {
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

	private static final class Predicated<A extends Annotation>
			extends Filtered<A> {
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

	private static final class Annotated<A extends Annotation>
			extends Filtered<A> {
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

	private static final class Typed<A extends Annotation, X extends A> extends AnnotationQuery<X> {
		private final AnnotationQuery<A> parent;
		private final Class<X> type;

		Typed(AnnotationQuery<A> parent, Class<X> type) {
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

	AnnotationQuery() {
		// package extension only
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

		private Empty() {
			// singleton
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
}
