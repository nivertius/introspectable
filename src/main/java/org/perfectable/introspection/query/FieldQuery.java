package org.perfectable.introspection.query;

import org.perfectable.introspection.InheritanceChain;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class FieldQuery extends MemberQuery<Field, FieldQuery> {

	public static <X> FieldQuery of(Class<X> type) {
		checkNotNull(type);
		return new Complete<>(type);
	}

	@Override
	public FieldQuery named(String name) {
		checkNotNull(name);
		return new Named(this, name);
	}

	@Override
	public FieldQuery filter(Predicate<? super Field> filter) {
		checkNotNull(filter);
		return new Predicated(this, filter);
	}

	@Override
	public FieldQuery typed(Class<?> type) {
		checkNotNull(type);
		return new Typed(this, type);
	}

	@Override
	public FieldQuery annotatedWith(AnnotationFilter annotationFilter) {
		checkNotNull(annotationFilter);
		return new Annotated(this, annotationFilter);
	}

	@Override
	public FieldQuery excludingModifier(int excludedModifier) {
		return new ExcludingModifier(this, excludedModifier);
	}

	private static final class Complete<X> extends FieldQuery {
		private final InheritanceChain<X> chain;

		Complete(Class<X> type) {
			this.chain = InheritanceChain.startingAt(type);
		}

		@Override
		public Stream<Field> stream() {
			return this.chain.stream()
					.flatMap(testedClass -> Stream.of(testedClass.getDeclaredFields()));
		}
	}

	private abstract static class Filtered extends FieldQuery {
		private final FieldQuery parent;

		Filtered(FieldQuery parent) {
			this.parent = parent;
		}

		protected abstract boolean matches(Field candidate);

		@Override
		public Stream<Field> stream() {
			return this.parent.stream()
					.filter(this::matches);
		}
	}

	private static final class Named extends Filtered {
		private final String name;

		Named(FieldQuery parent, String name) {
			super(parent);
			this.name = name;
		}

		@Override
		protected boolean matches(Field candidate) {
			return this.name.equals(candidate.getName());
		}
	}

	private static final class Predicated extends Filtered {
		private final Predicate<? super Field> filter;

		Predicated(FieldQuery parent, Predicate<? super Field> filter) {
			super(parent);
			this.filter = filter;
		}

		@Override
		protected boolean matches(Field candidate) {
			return this.filter.test(candidate);
		}
	}

	private static final class Typed extends Filtered {
		private final Class<?> type;

		Typed(FieldQuery parent, Class<?> type) {
			super(parent);
			this.type = type;
		}

		@Override
		protected boolean matches(Field candidate) {
			return this.type.isAssignableFrom(candidate.getType());
		}
	}

	private static final class Annotated extends Filtered {
		private final AnnotationFilter annotationFilter;

		Annotated(FieldQuery parent, AnnotationFilter annotationFilter) {
			super(parent);
			this.annotationFilter = annotationFilter;
		}

		@Override
		protected boolean matches(Field candidate) {
			return this.annotationFilter.appliesOn(candidate);
		}
	}

	private static final class ExcludingModifier extends Filtered {
		private final int excludedModifier;

		ExcludingModifier(FieldQuery parent, int excludedModifier) {
			super(parent);
			this.excludedModifier = excludedModifier;
		}

		@Override
		protected boolean matches(Field candidate) {
			return (candidate.getModifiers() & this.excludedModifier) == 0;
		}
	}

	protected FieldQuery() {
		// no default fields
	}

}
