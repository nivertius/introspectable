package org.perfectable.introspection.query; // SUPPRESS LENGTH

import java.io.Serializable;

import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.TypeFilterAssert.assertThat;

class TypeFilterTest { // SUPPRESS MethodCount

	@Test
	void all() {
		TypeFilter filter = TypeFilter.ALL;

		assertThat(filter)
			.matchesType(boolean.class)
			.matchesType(int.class)
			.matchesType(Number.class)
			.matchesType(Object.class)
			.matchesType(Serializable.class)
			.matchesType(Long.class)
			.matchesType(String.class);
	}

	@Test
	void none() {
		TypeFilter filter = TypeFilter.NONE;

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void noneWithExcluded() {
		TypeFilter filter = TypeFilter.NONE.withExcluded(Number.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void noneWithLowerBound() {
		TypeFilter filter = TypeFilter.NONE.withLowerBound(Number.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void noneWithUpperBound() {
		TypeFilter filter = TypeFilter.NONE.withUpperBound(Number.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void primitive() {
		TypeFilter filter = TypeFilter.PRIMITIVE;

		assertThat(filter)
			.matchesType(boolean.class)
			.matchesType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void primitiveWithLowerBoundReference() {
		TypeFilter filter = TypeFilter.PRIMITIVE.withLowerBound(String.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void primitiveWithLowerBoundPrimitive() {
		TypeFilter filter = TypeFilter.PRIMITIVE.withLowerBound(int.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.matchesType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void primitiveWithUpperBoundReference() {
		TypeFilter filter = TypeFilter.PRIMITIVE.withUpperBound(String.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void primitiveWithUpperBoundPrimitive() {
		TypeFilter filter = TypeFilter.PRIMITIVE.withUpperBound(int.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.matchesType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void primitiveWithExcludedReference() {
		TypeFilter filter = TypeFilter.PRIMITIVE.withExcluded(String.class);

		assertThat(filter)
			.matchesType(boolean.class)
			.matchesType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void primitiveWithExcludedPrimitive() {
		TypeFilter filter = TypeFilter.PRIMITIVE.withExcluded(int.class);

		assertThat(filter)
			.matchesType(boolean.class)
			.doesntMatchType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void withUpperBound() {
		TypeFilter filter = TypeFilter.ALL.withUpperBound(Number.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.matchesType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.matchesType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void withLowerBound() {
		TypeFilter filter = TypeFilter.ALL.withLowerBound(Number.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.matchesType(Number.class)
			.matchesType(Object.class)
			.matchesType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void withExcluded() {
		TypeFilter filter = TypeFilter.ALL.withExcluded(Number.class);

		assertThat(filter)
			.matchesType(boolean.class)
			.matchesType(int.class)
			.doesntMatchType(Number.class)
			.matchesType(Object.class)
			.matchesType(Serializable.class)
			.matchesType(Long.class)
			.matchesType(String.class);
	}

	@Test
	void withExcludedTwiceSame() {
		TypeFilter filter = TypeFilter.ALL.withExcluded(Number.class).withExcluded(Number.class);

		assertThat(filter)
			.matchesType(boolean.class)
			.matchesType(int.class)
			.doesntMatchType(Number.class)
			.matchesType(Object.class)
			.matchesType(Serializable.class)
			.matchesType(Long.class)
			.matchesType(String.class);
	}

	@Test
	void withExcludedTwiceDifferent() {
		TypeFilter filter = TypeFilter.ALL.withExcluded(Number.class).withExcluded(Serializable.class);

		assertThat(filter)
			.matchesType(boolean.class)
			.matchesType(int.class)
			.doesntMatchType(Number.class)
			.matchesType(Object.class)
			.doesntMatchType(Serializable.class)
			.matchesType(Long.class)
			.matchesType(String.class);
	}

	@Test
	void superTypeOf() {
		TypeFilter filter = TypeFilter.superTypeOf(Number.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.matchesType(Number.class)
			.matchesType(Object.class)
			.matchesType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void subtypeOf() {
		TypeFilter filter = TypeFilter.subtypeOf(Number.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.matchesType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.matchesType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void subtypeOfObject() {
		TypeFilter filter = TypeFilter.subtypeOf(Object.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.matchesType(Number.class)
			.matchesType(Object.class)
			.matchesType(Serializable.class)
			.matchesType(Long.class)
			.matchesType(String.class);
	}


	@Test
	void exact() {
		TypeFilter filter = TypeFilter.exact(Number.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.matchesType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void exactWithExcludedDifferent() {
		TypeFilter filter = TypeFilter.exact(Number.class).withExcluded(String.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.matchesType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void exactWithExcludedMatching() {
		TypeFilter filter = TypeFilter.exact(Number.class).withExcluded(Number.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void exactWithUpperBoundMatching() {
		TypeFilter filter = TypeFilter.exact(Number.class).withUpperBound(Object.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.matchesType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void exactWithUpperBoundMismatchingSubtype() {
		TypeFilter filter = TypeFilter.exact(Number.class).withUpperBound(Long.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void exactWithUpperBoundMismatchingDifferent() {
		TypeFilter filter = TypeFilter.exact(Number.class).withUpperBound(String.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void exactWithLowerBoundMatching() {
		TypeFilter filter = TypeFilter.exact(Number.class).withLowerBound(Long.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.matchesType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void exactWithLowerBoundMismatchingSupertype() {
		TypeFilter filter = TypeFilter.exact(Number.class).withLowerBound(Object.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void exactWithLowerBoundMismatchingDifferent() {
		TypeFilter filter = TypeFilter.exact(Number.class).withLowerBound(String.class);

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.doesntMatchType(int.class)
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void preselected() {
		TypeFilter filter = TypeFilter.exact(boolean.class)
			.or(TypeFilter.exact(Number.class))
			.or(TypeFilter.exact(Serializable.class));

		assertThat(filter)
			.matchesType(boolean.class)
			.doesntMatchType(int.class)
			.matchesType(Number.class)
			.doesntMatchType(Object.class)
			.matchesType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void preexcluded() {
		TypeFilter filter = TypeFilter.ALL.and(TypeFilter.exact(boolean.class).negated())
			.and(TypeFilter.exact(Number.class).negated())
			.and(TypeFilter.exact(Serializable.class).negated());

		assertThat(filter)
			.doesntMatchType(boolean.class)
			.matchesType(int.class)
			.doesntMatchType(Number.class)
			.matchesType(Object.class)
			.doesntMatchType(Serializable.class)
			.matchesType(Long.class)
			.matchesType(String.class);
	}


	@Test
	void custom() {
		TypeFilter filter = type -> type.equals(boolean.class) || type.equals(Number.class);

		assertThat(filter)
			.matchesType(boolean.class)
			.doesntMatchType(int.class)
			.matchesType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void customNegated() {
		TypeFilter positive = type -> type.equals(boolean.class) || type.equals(Number.class);
		TypeFilter negated = positive.negated();

		assertThat(negated)
			.doesntMatchType(boolean.class)
			.matchesType(int.class)
			.doesntMatchType(Number.class)
			.matchesType(Object.class)
			.matchesType(Serializable.class)
			.matchesType(Long.class)
			.matchesType(String.class);
	}
}
