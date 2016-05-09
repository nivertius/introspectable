package org.perfectable.introspection;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.Charset;

import org.junit.Test;

@SuppressWarnings("static-method")
public class RelatedClassesIterableTest {
	
	@Test
	public void testString() {
		RelatedClassesIterable related = RelatedClassesIterable.of(String.class);
		
		assertThat(related)
				.contains(int.class)
				.contains(void.class)
				.contains(StringBuffer.class)
				.contains(Charset.class)
				.hasSize(659);
	}
	
	@Test
	public void testThisTest() {
		RelatedClassesIterable related = RelatedClassesIterable.of(RelatedClassesIterableTest.class)
				.excluding(Object.class);
		
		assertThat(related)
				.containsExactly(void.class);
	}
}
