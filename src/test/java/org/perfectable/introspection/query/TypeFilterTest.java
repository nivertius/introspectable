package org.perfectable.introspection.query;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.TypeFilterAssert.assertThat;

// SUPPRESS NEXT 1 MethodCount
class TypeFilterTest {

	@Test
	void all() {
		TypeFilter filter = TypeFilter.ALL;

		assertThat(filter)
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
			.matchesType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.matchesType(Long.class)
			.doesntMatchType(String.class);
	}

	@Test
	void exact() {
		TypeFilter filter = TypeFilter.exact(Number.class);

		assertThat(filter)
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
			.doesntMatchType(Number.class)
			.doesntMatchType(Object.class)
			.doesntMatchType(Serializable.class)
			.doesntMatchType(Long.class)
			.doesntMatchType(String.class);
	}
}
