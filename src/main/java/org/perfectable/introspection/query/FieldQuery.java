package org.perfectable.introspection.query; // SUPPRESS FileLength

import org.perfectable.introspection.PrivilegedActions;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;

import static java.util.Objects.requireNonNull;

// SUPPRESS NEXT 1 ClassDataAbstractionCoupling
public abstract class FieldQuery extends MemberQuery<Field, FieldQuery> {

	public static FieldQuery empty() {
		return Empty.INSTANCE;
	}

	public static <X> FieldQuery of(Class<X> type) {
		requireNonNull(type);
		return new Complete<>(type);
	}

	public FieldQuery join(FieldQuery other) {
		requireNonNull(other);
		return Composite.composite(this, other);
	}

	@Override
	public FieldQuery named(String name) {
		requireNonNull(name);
		return new Named(this, name);
	}

	@Override
	public FieldQuery nameMatching(Pattern namePattern) {
		requireNonNull(namePattern);
		return new NameMatching(this, namePattern);
	}

	@Override
	public FieldQuery filter(Predicate<? super Field> filter) {
		requireNonNull(filter);
		return new Predicated(this, filter);
	}

	public FieldQuery typed(Class<?> type) {
		return typed(TypeFilter.subtypeOf(type));
	}

	public FieldQuery typed(TypeFilter typeFilter) {
		requireNonNull(typeFilter);
		return new Typed(this, typeFilter);
	}

	@Override
	public FieldQuery annotatedWith(AnnotationFilter annotationFilter) {
		requireNonNull(annotationFilter);
		return new Annotated(this, annotationFilter);
	}

	@Override
	public FieldQuery requiringModifier(int requiredModifier) {
		return new RequiringModifier(this, requiredModifier);
	}

	@Override
	public FieldQuery excludingModifier(int excludedModifier) {
		return new ExcludingModifier(this, excludedModifier);
	}

	@Override
	public FieldQuery asAccessible() {
		return new AccessibleMarking(this);
	}

	FieldQuery() {
		// package extension only
	}

	private static final class Complete<X> extends FieldQuery {
		private final InheritanceQuery<X> chain;

		Complete(Class<X> type) {
			this.chain = InheritanceQuery.of(type);
		}

		@Override
		public Stream<Field> stream() {
			return this.chain.stream()
				.flatMap(testedClass -> Stream.of(testedClass.getDeclaredFields()));
		}

		@Override
		public boolean contains(Object candidate) {
			if (!(candidate instanceof Field)) {
				return false;
			}
			Field candidateField = (Field) candidate;
			@SuppressWarnings("unchecked")
			Class<? super X> declaringClass = (Class<? super X>) candidateField.getDeclaringClass();
			return chain.contains(declaringClass);
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

		@Override
		public boolean contains(Object candidate) {
			if (!(candidate instanceof Field)) {
				return false;
			}
			Field candidateField = (Field) candidate;
			return matches(candidateField) && parent.contains(candidate);
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

	private static final class NameMatching extends Filtered {
		private final Pattern namePattern;

		NameMatching(FieldQuery parent, Pattern namePattern) {
			super(parent);
			this.namePattern = namePattern;
		}

		@Override
		protected boolean matches(Field candidate) {
			return this.namePattern.matcher(candidate.getName()).matches();
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
		private final TypeFilter typeFilter;

		Typed(FieldQuery parent, TypeFilter typeFilter) {
			super(parent);
			this.typeFilter = typeFilter;
		}

		@Override
		protected boolean matches(Field candidate) {
			return this.typeFilter.matches(candidate.getType());
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
			return this.annotationFilter.matches(candidate);
		}
	}

	private static final class RequiringModifier extends Filtered {
		private final int requiredModifier;

		RequiringModifier(FieldQuery parent, int requiredModifier) {
			super(parent);
			this.requiredModifier = requiredModifier;
		}

		@Override
		protected boolean matches(Field candidate) {
			return (candidate.getModifiers() & this.requiredModifier) != 0;
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

	private static class AccessibleMarking extends FieldQuery {
		private final FieldQuery parent;

		AccessibleMarking(FieldQuery parent) {
			this.parent = parent;
		}

		@Override
		public Stream<Field> stream() {
			return this.parent.stream()
				.peek(PrivilegedActions::markAccessible);
		}

		@Override
		public boolean contains(Object candidate) {
			return parent.contains(candidate);
		}
	}

	private static final class Empty extends FieldQuery {
		static final Empty INSTANCE = new Empty();

		@Override
		public Stream<Field> stream() {
			return Stream.of();
		}

		@Override
		public boolean contains(Object candidate) {
			return false;
		}

		private Empty() {
			// singleton
		}
	}

	private static final class Composite extends FieldQuery {
		private final ImmutableList<FieldQuery> components;

		private Composite(ImmutableList<FieldQuery> components) {
			this.components = components;
		}

		private static Composite composite(FieldQuery... components) {
			return new Composite(ImmutableList.copyOf(components));
		}

		@Override
		public Stream<Field> stream() {
			return components.stream().flatMap(FieldQuery::stream);
		}

		@Override
		public boolean contains(Object candidate) {
			return components.stream().anyMatch(component -> component.contains(candidate));
		}

		@Override
		public Composite join(FieldQuery other) {
			ImmutableList<FieldQuery> newComponents =
				ImmutableList.<FieldQuery>builder()
					.addAll(components).add(other).build();
			return new Composite(newComponents);
		}
	}
}
