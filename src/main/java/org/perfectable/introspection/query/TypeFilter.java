package org.perfectable.introspection.query;

@FunctionalInterface
public interface TypeFilter {
	TypeFilter ALL = TypeFilters.Complete.INSTANCE;
	TypeFilter NONE = TypeFilters.Empty.INSTANCE;
	TypeFilter PRIMITIVE = TypeFilters.Primitive.INSTANCE;

	static TypeFilter superTypeOf(Class<?> subType) {
		return ALL.withLowerBound(subType);
	}

	static TypeFilter subtypeOf(Class<?> superType) {
		return ALL.withUpperBound(superType);
	}

	static TypeFilter exact(Class<?> matchedType) {
		return new TypeFilters.Exact(matchedType);
	}

	boolean matches(Class<?> candidate);

	default TypeFilter withUpperBound(Class<?> superType) {
		return new TypeFilters.UpperBounded(this, superType);
	}

	default TypeFilter withLowerBound(Class<?> subType) {
		return new TypeFilters.LowerBounded(this, subType);
	}

	default TypeFilter withExcluded(Class<?> excludedType) {
		return new TypeFilters.Excluded(this, excludedType);
	}

	default TypeFilter negated() {
		return new TypeFilters.Negated(this);
	}
}
