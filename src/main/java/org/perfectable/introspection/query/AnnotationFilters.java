package org.perfectable.introspection.query;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;
import org.checkerframework.checker.nullness.qual.Nullable;

final class AnnotationFilters {
	private AnnotationFilters() {
		// utility
	}

	static final class Single<A extends Annotation> implements AnnotationFilter.Singular<A> {
		public static final Predicate<Annotation> ALWAYS_TRUE = element -> true;

		private final Class<A> annotationClass;
		private final Predicate<? super A> predicate;

		public static <A extends Annotation> Single<A> create(Class<A> annotationClass) {
			return new Single<>(annotationClass, ALWAYS_TRUE);
		}

		private Single(Class<A> annotationClass, Predicate<? super A> predicate) {
			this.annotationClass = annotationClass;
			this.predicate = predicate;
		}

		@Override
		public Singular<A> andMatching(Predicate<? super A> addedPredicate) {
			@SuppressWarnings("unchecked")
			Predicate<? super A> newPredicate = ((Predicate<A>) this.predicate).and(addedPredicate);
			return new Single<A>(this.annotationClass, newPredicate);
		}

		@Override
		public boolean matches(AnnotatedElement element) {
			@Nullable A annotation = element.getAnnotation(this.annotationClass);
			if (annotation == null) {
				return false;
			}
			return this.predicate.test(annotation);
		}
	}

	static final class Accepting implements AnnotationFilter {
		public static final AnnotationFilter INSTANCE = new Accepting();

		private Accepting() {
			// singleton
		}

		@Override
		public boolean matches(AnnotatedElement element) {
			return true;
		}
	}

	static final class Rejecting implements AnnotationFilter {
		public static final AnnotationFilter INSTANCE = new Rejecting();

		private Rejecting() {
			// singleton
		}

		@Override
		public boolean matches(AnnotatedElement element) {
			return false;
		}
	}

	static final class Absent implements AnnotationFilter {
		public static final AnnotationFilter INSTANCE = new Absent();

		private Absent() {
			// singleton
		}

		@Override
		public boolean matches(AnnotatedElement element) {
			return element.getAnnotations().length == 0;
		}
	}

	static final class Negated implements AnnotationFilter {
		private final AnnotationFilter positive;

		Negated(AnnotationFilter positive) {
			this.positive = positive;
		}

		@Override
		public boolean matches(AnnotatedElement candidate) {
			return !positive.matches(candidate);
		}

		@Override
		public AnnotationFilter negated() {
			return positive;
		}

		@Override
		public AnnotationFilter or(AnnotationFilter other) {
			if (other instanceof Negated) {
				return positive.and(((Negated) other).positive).negated();
			}
			return AnnotationFilter.super.or(other);
		}

		@Override
		public AnnotationFilter and(AnnotationFilter other) {
			if (other instanceof Negated) {
				return positive.or(((Negated) other).positive).negated();
			}
			return AnnotationFilter.super.and(other);
		}
	}

	static final class Disjunction implements AnnotationFilter {
		private final ImmutableSet<AnnotationFilter> components;

		static Disjunction create(AnnotationFilter... components) {
			return new Disjunction(ImmutableSet.copyOf(components));
		}

		private Disjunction(ImmutableSet<AnnotationFilter> components) {
			this.components = components;
		}

		@Override
		public boolean matches(AnnotatedElement candidate) {
			return components.stream().anyMatch(component -> component.matches(candidate));
		}

		@Override
		public AnnotationFilter or(AnnotationFilter other) {
			ImmutableSet<AnnotationFilter> newComponents = ImmutableSet.<AnnotationFilter>builder()
				.addAll(components).add(other).build();
			return new Disjunction(newComponents);
		}
	}

	static final class Conjunction implements AnnotationFilter {
		private final ImmutableSet<AnnotationFilter> components;

		static Conjunction create(AnnotationFilter... components) {
			return new Conjunction(ImmutableSet.copyOf(components));
		}

		private Conjunction(ImmutableSet<AnnotationFilter> components) {
			this.components = components;
		}

		@Override
		public boolean matches(AnnotatedElement candidate) {
			return components.stream().allMatch(component -> component.matches(candidate));
		}

		@Override
		public AnnotationFilter and(AnnotationFilter other) {
			ImmutableSet<AnnotationFilter> newComponents = ImmutableSet.<AnnotationFilter>builder()
				.addAll(components).add(other).build();
			return new Conjunction(newComponents);
		}
	}
}
