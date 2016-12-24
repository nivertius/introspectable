package org.perfectable.introspection;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("static-method")
public class RelatedClassesIterableTest {

	@Test
	public void testString() {
		RelatedClassesIterable related = RelatedClassesIterable.of(String.class);

		assertThat(related)
				.contains(int.class)
				.contains(void.class)
				.contains(StringBuffer.class)
				.contains(Charset.class);
	}

	@Test
	public void testThisTest() {
		RelatedClassesIterable related = RelatedClassesIterable.of(RelatedClassesIterableTest.class)
				.excluding(Object.class);

		assertThat(related)
				.containsExactly(void.class);
	}
}