package org.perfectable.introspection.query;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GenericsQueryTest {

	@Test
	public void testNonGeneric() {
		GenericsQuery<String> query = GenericsQuery.of(String.class);

		assertThatThrownBy(() -> query.parameter(0))
				.hasNoCause()
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	public void testGenericWithoutBound() {
		Class<?> result = GenericsQuery.of(BoundedInterface.class).parameter(0)
				.resolve(UnboundedImplementation.class);

		assertThat(result)
				.isEqualTo(Number.class);
	}

	@Test
	public void testGenericWithoutBoundInstance() {
		Class<?> result = GenericsQuery.of(BoundedInterface.class).parameter(0)
				.resolve(new UnboundedImplementation<>());

		assertThat(result)
				.isEqualTo(Number.class);
	}

	@Test
	public void testGenericWithBound() {
		Class<?> result = GenericsQuery.of(BoundedInterface.class).parameter(0)
				.resolve(BoundedImplementation.class);

		assertThat(result)
				.isEqualTo(Long.class);
	}

	@Test
	public void testGenericWithBoundInstance() {
		Class<?> result = GenericsQuery.of(BoundedInterface.class).parameter(0)
				.resolve(new BoundedImplementation());

		assertThat(result)
				.isEqualTo(Long.class);
	}

	interface BoundedInterface<X extends Number> {
		// test interface
	}

	static class UnboundedImplementation<X extends Number> implements BoundedInterface<X> {
		// test interface
	}

	static class BoundedImplementation implements BoundedInterface<Long> {
		// test interface
	}

}
