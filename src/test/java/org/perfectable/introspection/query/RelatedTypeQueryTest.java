package org.perfectable.introspection.query;

import org.perfectable.introspection.Subject;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RelatedTypeQueryTest {

	private static final String MAIN_PACKAGE = "org.perfectable";

	@Test
	void testString() {
		RelatedTypeQuery related = RelatedTypeQuery.of(String.class);

		assertThat(related)
				.doesNotHaveDuplicates()
				.contains(int.class)
				.contains(void.class)
				.contains(StringBuffer.class)
				.contains(Charset.class);
	}

	@Test
	void testSubjectAnnotation() {
		RelatedTypeQuery related = RelatedTypeQuery.of(Subject.Special.class);

		assertThat(related)
				.doesNotHaveDuplicates();
		assertThat(related)
				.filteredOn(type -> type.getPackage() != null)
				.filteredOn(type -> type.getPackage().getName().startsWith(MAIN_PACKAGE))
				.containsExactlyInAnyOrder(Subject.class, Subject.Special.class, Subject.OtherAnnotation.class);
	}

	@Test
	void testSubjectFiltered() {
		RelatedTypeQuery related = RelatedTypeQuery.of(Subject.Special.class)
				.filter(type -> type.getPackage() != null)
				.filter(type -> type.getPackage().getName().startsWith(MAIN_PACKAGE));

		assertThat(related)
				.doesNotHaveDuplicates();
		assertThat(related)
				.containsExactlyInAnyOrder(Subject.class, Subject.Special.class, Subject.OtherAnnotation.class);
	}
}
