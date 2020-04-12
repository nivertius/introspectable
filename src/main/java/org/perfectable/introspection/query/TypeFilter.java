package org.perfectable.introspection.query;

import java.lang.reflect.Type;

@FunctionalInterface
public interface TypeFilter {
	TypeFilter ALL = TypeFilters.Complete.INSTANCE;
	TypeFilter NONE = TypeFilters.Empty.INSTANCE;
	TypeFilter PRIMITIVE = TypeFilters.Primitive.INSTANCE;

	static TypeFilter superTypeOf(Type subType) {
		return ALL.withLowerBound(subType);
	}

	static TypeFilter subtypeOf(Type superType) {
		return ALL.withUpperBound(superType);
	}

	static TypeFilter exact(Type matchedType) {
		return new TypeFilters.Exact(matchedType);
	}

	boolean matches(Type candidate);

	default TypeFilter withUpperBound(Type superType) {
		return new TypeFilters.UpperBounded(this, superType);
	}

	default TypeFilter withLowerBound(Type subType) {
		return new TypeFilters.LowerBounded(this, subType);
	}

	default TypeFilter withExcluded(Type excludedType) {
		return and(exact(excludedType).negated());
	}

	default TypeFilter negated() {
		return new TypeFilters.Negated(this);
	}

	default TypeFilter and(TypeFilter other) {
		return TypeFilters.Conjunction.create(this, other);
	}

	default TypeFilter or(TypeFilter other) {
		return TypeFilters.Disjunction.create(this, other);
	}
}
