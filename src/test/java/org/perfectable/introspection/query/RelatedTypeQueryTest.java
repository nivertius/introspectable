package org.perfectable.introspection.query;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.AbstractQueryAssert.assertThat;

class RelatedTypeQueryTest {
	private static final String MAIN_PACKAGE = "org.perfectable";

	@Test
	void testString() {
		RelatedTypeQuery related = RelatedTypeQuery.of(String.class);

		assertThat(related)
			.doesNotHaveDuplicates()
			.contains(int.class, void.class, StringBuffer.class, Charset.class);
	}

	@Test
	void testSubjectAnnotation() {
		RelatedTypeQuery related = RelatedTypeQuery.of(Subject.Special.class);

		assertThat(related)
			.doesNotHaveDuplicates()
			.filteredOn(type -> type.getPackage() != null)
			.filteredOn(type -> type.getPackage().getName().startsWith(MAIN_PACKAGE))
			.containsExactly(Subject.class, Subject.Special.class, Subject.OtherAnnotation.class,
				Subject.NestedInterface.class);
	}

	@Test
	void testSubjectFiltered() {
		RelatedTypeQuery related = RelatedTypeQuery.of(Subject.Special.class)
			.filter(type -> type.getPackage() != null)
			.filter(type -> type.getPackage().getName().startsWith(MAIN_PACKAGE));

		assertThat(related)
			.doesNotHaveDuplicates()
			.containsExactly(Subject.class, Subject.Special.class, Subject.OtherAnnotation.class,
				Subject.NestedInterface.class);
	}
}
