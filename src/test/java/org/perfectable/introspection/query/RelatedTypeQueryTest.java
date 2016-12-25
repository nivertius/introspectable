package org.perfectable.introspection.query;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("static-method")
public class RelatedTypeQueryTest {

	@Test
	public void testString() {
		RelatedTypeQuery related = RelatedTypeQuery.of(String.class);

		assertThat(related)
				.contains(int.class)
				.contains(void.class)
				.contains(StringBuffer.class)
				.contains(Charset.class);
	}
}
