package org.perfectable.introspection.query;

import com.google.common.collect.ImmutableSet;

final class TypeFilters {

	private TypeFilters() {
		// utility
	}

	static final class Primitive implements TypeFilter {
		static final Primitive INSTANCE = new Primitive();

		private Primitive() {
			// singleton
		}

		@Override
		public TypeFilter withLowerBound(Class<?> subType) {
			if (subType.isPrimitive()) {
				return new Exact(subType);
			}
			return NONE;
		}

		@Override
		public TypeFilter withUpperBound(Class<?> superType) {
			if (superType.isPrimitive()) {
				return new Exact(superType);
			}
			return NONE;
		}

		@Override
		public TypeFilter withExcluded(Class<?> excludedType) {
			if (!excludedType.isPrimitive()) {
				return this;
			}
			return TypeFilter.super.withExcluded(excludedType);
		}

		@Override
		public boolean matches(Class<?> candidate) {
			return candidate.isPrimitive();
		}
	}

	static final class Complete implements TypeFilter {
		static final Complete INSTANCE = new Complete();

		private Complete() {
			// singleton
		}

		@Override
		public boolean matches(Class<?> candidate) {
			return true;
		}

		@Override
		public TypeFilter negated() {
			return NONE;
		}
	}

	static final class Empty implements TypeFilter {
		static final Empty INSTANCE = new Empty();

		private Empty() {
			// singleton
		}

		@Override
		public boolean matches(Class<?> candidate) {
			return false;
		}

		@Override
		public TypeFilter withExcluded(Class<?> excludedType) {
			return INSTANCE;
		}

		@Override
		public TypeFilter withLowerBound(Class<?> subType) {
			return INSTANCE;
		}

		@Override
		public TypeFilter withUpperBound(Class<?> superType) {
			return INSTANCE;
		}

		@Override
		public TypeFilter negated() {
			return TypeFilter.ALL;
		}
	}

	static final class Exact implements TypeFilter {
		private final Class<?> matchedType;

		Exact(Class<?> matchedType) {
			this.matchedType = matchedType;
		}

		@Override
		public boolean matches(Class<?> candidate) {
			return matchedType.equals(candidate);
		}

		@Override
		public TypeFilter withExcluded(Class<?> excludedType) {
			if (excludedType.equals(matchedType)) {
				return NONE;
			}
			return this;
		}

		@Override
		public TypeFilter withUpperBound(Class<?> superType) {
			if (!superType.isAssignableFrom(matchedType)) {
				return NONE;
			}
			return this;
		}

		@Override
		public TypeFilter withLowerBound(Class<?> subType) {
			if (!matchedType.isAssignableFrom(subType)) {
				return NONE;
			}
			return this;
		}
	}

	abstract static class Filtered implements TypeFilter {
		private final TypeFilter parent;

		Filtered(TypeFilter parent) {
			this.parent = parent;
		}

		@Override
		public boolean matches(Class<?> candidate) {
			return parent.matches(candidate) && concreteMatches(candidate);
		}

		protected abstract boolean concreteMatches(Class<?> candidate);
	}

	static final class UpperBounded extends Filtered {
		private final Class<?> subType;

		UpperBounded(TypeFilter parent, Class<?> subType) {
			super(parent);
			this.subType = subType;
		}

		@Override
		protected boolean concreteMatches(Class<?> candidate) {
			return subType.isAssignableFrom(candidate);
		}
	}

	static final class LowerBounded extends Filtered {
		private final Class<?> superType;

		LowerBounded(TypeFilter parent, Class<?> superType) {
			super(parent);
			this.superType = superType;
		}

		@Override
		protected boolean concreteMatches(Class<?> candidate) {
			return candidate.isAssignableFrom(superType);
		}
	}

	static final class Negated implements TypeFilter {
		private final TypeFilter positive;

		Negated(TypeFilter positive) {
			this.positive = positive;
		}

		@Override
		public boolean matches(Class<?> candidate) {
			return !positive.matches(candidate);
		}

		@Override
		public TypeFilter negated() {
			return positive;
		}
	}

	static final class Disjunction implements TypeFilter {
		private final ImmutableSet<TypeFilter> components;

		static Disjunction create(TypeFilter... components) {
			return new Disjunction(ImmutableSet.copyOf(components));
		}

		private Disjunction(ImmutableSet<TypeFilter> components) {
			this.components = components;
		}

		@Override
		public boolean matches(Class<?> candidate) {
			return components.stream().anyMatch(component -> component.matches(candidate));
		}
	}

	static final class Conjunction implements TypeFilter {
		private final ImmutableSet<TypeFilter> components;

		static Conjunction create(TypeFilter... components) {
			return new Conjunction(ImmutableSet.copyOf(components));
		}

		private Conjunction(ImmutableSet<TypeFilter> components) {
			this.components = components;
		}

		@Override
		public boolean matches(Class<?> candidate) {
			return components.stream().allMatch(component -> component.matches(candidate));
		}
	}
}
