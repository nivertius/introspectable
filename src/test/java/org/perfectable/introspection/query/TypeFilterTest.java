package org.perfectable.introspection.query;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

// SUPPRESS NEXT 1 MethodCount
class TypeFilterTest {
	@Test
	void all() {
		TypeFilter filter = TypeFilter.ALL;

		assertThat(filter.matches(Number.class)).isTrue();
		assertThat(filter.matches(Object.class)).isTrue();
		assertThat(filter.matches(Serializable.class)).isTrue();
		assertThat(filter.matches(Long.class)).isTrue();
		assertThat(filter.matches(String.class)).isTrue();
	}

	@Test
	void none() {
		TypeFilter filter = TypeFilter.NONE;

		assertThat(filter.matches(Number.class)).isFalse();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void noneWithExcluded() {
		TypeFilter filter = TypeFilter.NONE.withExcluded(Number.class);

		assertThat(filter.matches(Number.class)).isFalse();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void noneWithLowerBound() {
		TypeFilter filter = TypeFilter.NONE.withLowerBound(Number.class);

		assertThat(filter.matches(Number.class)).isFalse();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void noneWithUpperBound() {
		TypeFilter filter = TypeFilter.NONE.withUpperBound(Number.class);

		assertThat(filter.matches(Number.class)).isFalse();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void withUpperBound() {
		TypeFilter filter = TypeFilter.ALL.withUpperBound(Number.class);

		assertThat(filter.matches(Number.class)).isTrue();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isTrue();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void withLowerBound() {
		TypeFilter filter = TypeFilter.ALL.withLowerBound(Number.class);

		assertThat(filter.matches(Number.class)).isTrue();
		assertThat(filter.matches(Object.class)).isTrue();
		assertThat(filter.matches(Serializable.class)).isTrue();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void withExcluded() {
		TypeFilter filter = TypeFilter.ALL.withExcluded(Number.class);

		assertThat(filter.matches(Number.class)).isFalse();
		assertThat(filter.matches(Object.class)).isTrue();
		assertThat(filter.matches(Serializable.class)).isTrue();
		assertThat(filter.matches(Long.class)).isTrue();
		assertThat(filter.matches(String.class)).isTrue();
	}

	@Test
	void withExcludedTwiceSame() {
		TypeFilter filter = TypeFilter.ALL.withExcluded(Number.class).withExcluded(Number.class);

		assertThat(filter.matches(Number.class)).isFalse();
		assertThat(filter.matches(Object.class)).isTrue();
		assertThat(filter.matches(Serializable.class)).isTrue();
		assertThat(filter.matches(Long.class)).isTrue();
		assertThat(filter.matches(String.class)).isTrue();
	}

	@Test
	void withExcludedTwiceDifferent() {
		TypeFilter filter = TypeFilter.ALL.withExcluded(Number.class).withExcluded(Serializable.class);

		assertThat(filter.matches(Number.class)).isFalse();
		assertThat(filter.matches(Object.class)).isTrue();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isTrue();
		assertThat(filter.matches(String.class)).isTrue();
	}

	@Test
	void superTypeOf() {
		TypeFilter filter = TypeFilter.superTypeOf(Number.class);

		assertThat(filter.matches(Number.class)).isTrue();
		assertThat(filter.matches(Object.class)).isTrue();
		assertThat(filter.matches(Serializable.class)).isTrue();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void subtypeOf() {
		TypeFilter filter = TypeFilter.subtypeOf(Number.class);

		assertThat(filter.matches(Number.class)).isTrue();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isTrue();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void exact() {
		TypeFilter filter = TypeFilter.exact(Number.class);

		assertThat(filter.matches(Number.class)).isTrue();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void exactWithExcludedDifferent() {
		TypeFilter filter = TypeFilter.exact(Number.class).withExcluded(String.class);

		assertThat(filter.matches(Number.class)).isTrue();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void exactWithExcludedMatching() {
		TypeFilter filter = TypeFilter.exact(Number.class).withExcluded(Number.class);

		assertThat(filter.matches(Number.class)).isFalse();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void exactWithUpperBoundMatching() {
		TypeFilter filter = TypeFilter.exact(Number.class).withUpperBound(Object.class);

		assertThat(filter.matches(Number.class)).isTrue();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void exactWithUpperBoundMismatchingSubtype() {
		TypeFilter filter = TypeFilter.exact(Number.class).withUpperBound(Long.class);

		assertThat(filter.matches(Number.class)).isFalse();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void exactWithUpperBoundMismatchingDifferent() {
		TypeFilter filter = TypeFilter.exact(Number.class).withUpperBound(String.class);

		assertThat(filter.matches(Number.class)).isFalse();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void exactWithLowerBoundMatching() {
		TypeFilter filter = TypeFilter.exact(Number.class).withLowerBound(Long.class);

		assertThat(filter.matches(Number.class)).isTrue();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void exactWithLowerBoundMismatchingSupertype() {
		TypeFilter filter = TypeFilter.exact(Number.class).withLowerBound(Object.class);

		assertThat(filter.matches(Number.class)).isFalse();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}

	@Test
	void exactWithLowerBoundMismatchingDifferent() {
		TypeFilter filter = TypeFilter.exact(Number.class).withLowerBound(String.class);

		assertThat(filter.matches(Number.class)).isFalse();
		assertThat(filter.matches(Object.class)).isFalse();
		assertThat(filter.matches(Serializable.class)).isFalse();
		assertThat(filter.matches(Long.class)).isFalse();
		assertThat(filter.matches(String.class)).isFalse();
	}
}
