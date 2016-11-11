package org.perfectable.introspection.query;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SuppressWarnings("static-method")
public class GenericsQueryTest {

	@Test
	public void testNongeneric() {
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
	public void testGenericWithBound() {
		Class<?> result = GenericsQuery.of(BoundedInterface.class).parameter(0)
				.resolve(BoundedImplementation.class);

		assertThat(result)
				.isEqualTo(Long.class);
	}

	@SuppressWarnings("unused")
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
