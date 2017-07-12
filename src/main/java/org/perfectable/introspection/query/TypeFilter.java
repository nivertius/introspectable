package org.perfectable.introspection.query;

import static java.util.Objects.requireNonNull;

@FunctionalInterface
public interface TypeFilter {
	TypeFilter ALL = Complete.INSTANCE;
	TypeFilter NONE = Empty.INSTANCE;

	boolean matches(Class<?> candidate);

	default TypeFilter withUpperBound(Class<?> superType) {
		return new UpperBounded(this, superType);
	}

	default TypeFilter withLowerBound(Class<?> subType) {
		return new LowerBounded(this, subType);
	}

	default TypeFilter withExcluded(Class<?> excludedType) {
		return new Excluded(this, excludedType);
	}

	static TypeFilter superTypeOf(Class<?> subType) {
		return ALL.withLowerBound(subType);
	}

	static TypeFilter subtypeOf(Class<?> superType) {
		return ALL.withUpperBound(superType);
	}

	static TypeFilter exact(Class<?> matchedType) {
		requireNonNull(matchedType);
		return new Exact(matchedType);
	}

	final class Complete implements TypeFilter {
		static final Complete INSTANCE = new Complete();

		private Complete() {
			// singleton
		}

		@Override
		public boolean matches(Class<?> candidate) {
			return true;
		}
	}

	final class Empty implements TypeFilter {
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
	}

	final class Exact implements TypeFilter {
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

	abstract class Filtered implements TypeFilter {
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

	final class UpperBounded extends Filtered {
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

	final class LowerBounded extends Filtered {
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

	final class Excluded extends Filtered {
		private final Class<?> excludedType;

		Excluded(TypeFilter parent, Class<?> excludedType) {
			super(parent);
			this.excludedType = excludedType;
		}

		@Override
		protected boolean concreteMatches(Class<?> candidate) {
			return !excludedType.equals(candidate);
		}

		@Override
		public TypeFilter withExcluded(Class<?> newExcludedType) {
			if (excludedType.equals(newExcludedType)) {
				return this;
			}
			return super.withExcluded(newExcludedType);
		}
	}
}
