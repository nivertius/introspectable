package org.perfectable.introspection.query;

import java.io.Serializable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.AbstractQueryAssert.assertThat;

class InheritanceQueryTest {

	@Retention(RetentionPolicy.RUNTIME)
	private @interface Example {
		// marker
	}

	@Example
	private static class Root {
		// test class
	}

	private static class Branch extends Root {
		// test class
	}

	private static class Leaf extends Branch {
		// test class
	}

	@Test
	void testString() {
		InheritanceQuery<String> chain = InheritanceQuery.of(String.class);

		assertThat(chain)
			.containsExactly(String.class, Serializable.class, Comparable.class, CharSequence.class, Object.class);
	}

	@Test
	void testStringOnlyInterfaces() {
		InheritanceQuery<String> chain = InheritanceQuery.of(String.class).onlyInterfaces();

		assertThat(chain)
			.containsExactly(Serializable.class, Comparable.class, CharSequence.class);
	}

	@Test
	void testStringOnlyClasses() {
		InheritanceQuery<String> chain = InheritanceQuery.of(String.class).onlyClasses();

		assertThat(chain)
			.containsExactly(String.class, Object.class);
	}

	@Test
	void testFilter() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class)
			.filter(type -> type.getPackage().getName().startsWith("org.perfectable"));

		assertThat(chain)
			.containsExactly(Leaf.class, Branch.class, Root.class);
	}

	@Test
	void testAnnotatedWith() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class)
			.annotatedWith(Example.class);

		assertThat(chain)
			.hasUnique(Root.class)
			.containsExactly(Root.class);
	}

	@Test
	void testLeaf() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class);

		assertThat(chain)
			.containsExactly(Leaf.class, Branch.class, Root.class, Object.class);
	}

	@Test
	void testExcludingRoot() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class).upToExcluding(Root.class);

		assertThat(chain)
			.containsExactly(Leaf.class, Branch.class);
	}

	@Test
	void testIncludingRoot() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class).upToIncluding(Root.class);

		assertThat(chain)
			.containsExactly(Leaf.class, Branch.class, Root.class);
	}
}
