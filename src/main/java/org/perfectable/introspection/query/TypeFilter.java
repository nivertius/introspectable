package org.perfectable.introspection.query;

import java.lang.reflect.Type;

/**
 * Predicate-like interface for matching {@link Type}.
 */
@FunctionalInterface
public interface TypeFilter {
	/** Filter that matches any {@link Type}. */
	TypeFilter ALL = TypeFilters.Complete.INSTANCE;

	/** Filter that doesn't match any {@link Type}. */
	TypeFilter NONE = TypeFilters.Empty.INSTANCE;

	/** Filter that matches only primitive types. */
	TypeFilter PRIMITIVE = TypeFilters.Primitive.INSTANCE;

	/**
	 * Filter that matches only types that are supertype of specified one.
	 *
	 * <p>Note that any type is supertype of itself.
	 *
	 * @param subType type that is lower bound for this filter
	 * @return type filter that matches supertypes
	 */
	static TypeFilter superTypeOf(Type subType) {
		return ALL.withLowerBound(subType);
	}

	/**
	 * Filter that matches only types that are subtype of specified one.
	 *
	 * <p>Note that any type is subtype of itself.
	 *
	 * @param superType type that is upper bound for this filter
	 * @return type filter that matches subtypes
	 */
	static TypeFilter subtypeOf(Type superType) {
		return ALL.withUpperBound(superType);
	}

	/**
	 * Filter that matches exactly specified type and nothing else.
	 *
	 * @param matchedType type that is matched
	 * @return type filter that matches only provided type
	 */
	static TypeFilter exact(Type matchedType) {
		return new TypeFilters.Exact(matchedType);
	}

	/**
	 * Checks if type matches the filter.
	 *
	 * @param candidate type to check
	 * @return if filter matches
	 */
	boolean matches(Type candidate);

	/**
	 * Creates filter that matches only when this filter matches, and the matched type is subtype of provided one.
	 *
	 * @param superType type that is upper bound for this filter
	 * @return filter that matches both this filter and supertype
	 */
	default TypeFilter withUpperBound(Type superType) {
		return new TypeFilters.UpperBounded(this, superType);
	}

	/**
	 * Creates filter that matches only when this filter matches, and the matched type is supertype of provided one.
	 *
	 * @param subType type that is lower bound for this filter
	 * @return filter that matches both this filter and subtype
	 */
	default TypeFilter withLowerBound(Type subType) {
		return new TypeFilters.LowerBounded(this, subType);
	}

	/**
	 * Creates filter that matches everything that this one matches, but not exactly the provided type.
	 *
	 * @param excludedType type that will not be matched in resulting type
	 * @return filter that matches when this matches, but not the excluded type
	 */
	default TypeFilter withExcluded(Type excludedType) {
		return and(exact(excludedType).negated());
	}

	/**
	 * Creates filter negation.
	 *
	 * @return filter that matches only when this filter doesn't
	 */
	default TypeFilter negated() {
		return new TypeFilters.Negated(this);
	}

	/**
	 * Creates conjunction between this and provided filter.
	 *
	 * @param other additional filter to be checked
	 * @return filter that matches only when this and other filter matches
	 */
	default TypeFilter and(TypeFilter other) {
		return TypeFilters.Conjunction.create(this, other);
	}

	/**
	 * Creates disjunction between this and provided filter.
	 *
	 * @param other additional filter to be checked
	 * @return filter that matches whenever this or other filter matches
	 */
	default TypeFilter or(TypeFilter other) {
		return TypeFilters.Disjunction.create(this, other);
	}
}
