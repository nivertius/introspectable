package org.perfectable.introspection.query;

import org.perfectable.introspection.Subject;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class RelatedTypeQueryTest {

	@Test
	public void testString() {
		RelatedTypeQuery related = RelatedTypeQuery.of(String.class);

		assertThat(related)
				.doesNotHaveDuplicates()
				.contains(int.class)
				.contains(void.class)
				.contains(StringBuffer.class)
				.contains(Charset.class);
	}

	@Test
	public void testSubjectAnnotation() {
		RelatedTypeQuery related = RelatedTypeQuery.of(Subject.Special.class);

		assertThat(related)
				.doesNotHaveDuplicates();
		assertThat(related)
				.filteredOn(c -> c.getPackage() != null)
				.filteredOn(c -> c.getPackage().getName().startsWith("org.perfectable"))
				.containsExactlyInAnyOrder(Subject.class, Subject.Special.class, Subject.OtherAnnotation.class);
	}

	@Test
	public void testSubjectFiltered() {
		RelatedTypeQuery related = RelatedTypeQuery.of(Subject.Special.class)
				.filter(c -> c.getPackage() != null)
				.filter(c -> c.getPackage().getName().startsWith("org.perfectable"));

		assertThat(related)
				.doesNotHaveDuplicates();
		assertThat(related)
				.containsExactlyInAnyOrder(Subject.class, Subject.Special.class, Subject.OtherAnnotation.class);
	}
}
