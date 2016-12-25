package org.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class AnnotationQuery<A extends Annotation>
		extends AbstractQuery<A, AnnotationQuery<A>> {

	public static AnnotationQuery<Annotation> empty() {
		return EmptyAnnotationQuery.INSTANCE;
	}

	public static AnnotationQuery<Annotation> of(AnnotatedElement element) {
		return new ElementAnnotationQuery(element);
	}

	@SuppressWarnings("unchecked")
	public AnnotationQuery<A> join(AnnotationQuery<? extends A> other) {
		return CompositeAnnotationQuery.composite(this, other);
	}

	@Override
	public AnnotationQuery<A> filter(Predicate<? super A> filter) {
		checkNotNull(filter);
		return new PredicatedAnnotationQuery<>(this, filter);
	}

	public <X extends A> AnnotationQuery<X> typed(Class<X> annotationClass) {
		checkNotNull(annotationClass);
		return new TypedAnnotationQuery<>(this, annotationClass);
	}

	private static final class ElementAnnotationQuery extends AnnotationQuery<Annotation> {
		private final AnnotatedElement element;

		ElementAnnotationQuery(AnnotatedElement element) {
			this.element = element;
		}

		@Override
		public Stream<Annotation> stream() {
			return Stream.of(element.getAnnotations());
		}
	}

	private abstract static class FilteredAnnotationQuery<A extends Annotation>
			extends AnnotationQuery<A> {
		private final AnnotationQuery<A> parent;

		FilteredAnnotationQuery(AnnotationQuery<A> parent) {
			this.parent = parent;
		}

		protected abstract boolean matches(A candidate);

		@Override
		public Stream<A> stream() {
			return this.parent.stream()
					.filter(this::matches);
		}
	}

	private static final class PredicatedAnnotationQuery<A extends Annotation>
			extends FilteredAnnotationQuery<A> {
		private final Predicate<? super A> filter;

		PredicatedAnnotationQuery(AnnotationQuery<A> parent, Predicate<? super A> filter) {
			super(parent);
			this.filter = filter;
		}

		@Override
		protected boolean matches(A candidate) {
			return filter.test(candidate);
		}
	}

	private static final class TypedAnnotationQuery<A extends Annotation, X extends A> extends AnnotationQuery<X> {
		private final AnnotationQuery<A> parent;
		private final Class<X> type;

		TypedAnnotationQuery(AnnotationQuery<A> parent, Class<X> type) {
			this.parent = parent;
			this.type = type;
		}

		@Override
		public Stream<X> stream() {
			return parent.stream()
					.filter(type::isInstance)
					.map(type::cast);
		}
	}


	AnnotationQuery() {
		// package extension only
	}

	private static final class EmptyAnnotationQuery extends AnnotationQuery<Annotation> {
		private static final EmptyAnnotationQuery INSTANCE = new EmptyAnnotationQuery();

		@Override
		public Stream<Annotation> stream() {
			return Stream.empty();
		}

		@Override
		public EmptyAnnotationQuery filter(Predicate<? super Annotation> filter) {
			return EmptyAnnotationQuery.INSTANCE;
		}

		@SuppressWarnings("unchecked")
		@Override
		public AnnotationQuery<Annotation> join(AnnotationQuery<? extends Annotation> other) {
			return (AnnotationQuery<Annotation>) other;
		}

		private EmptyAnnotationQuery() {
			// singleton
		}
	}

	private static final class CompositeAnnotationQuery<A extends Annotation> extends AnnotationQuery<A> {
		private final ImmutableList<AnnotationQuery<? extends A>> components;

		private CompositeAnnotationQuery(ImmutableList<AnnotationQuery<? extends A>> components) {
			this.components = components;
		}

		@SuppressWarnings("unchecked")
		private static <A extends Annotation> AnnotationQuery<A> composite(AnnotationQuery<? extends A>... components) {
			return new CompositeAnnotationQuery<>(ImmutableList.copyOf(components));
		}

		@Override
		public Stream<A> stream() {
			return components.stream().flatMap(component -> component.stream());
		}

		@Override
		public AnnotationQuery<A> join(AnnotationQuery<? extends A> other) {
			ImmutableList<AnnotationQuery<? extends A>> newComponents =
					ImmutableList.<AnnotationQuery<? extends A>>builder()
						.addAll(components).add(other).build();
			return new CompositeAnnotationQuery<>(newComponents);
		}
	}
}
