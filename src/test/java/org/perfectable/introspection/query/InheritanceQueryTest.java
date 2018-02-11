package org.perfectable.introspection.query;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

import static org.perfectable.introspection.query.AbstractQueryAssert.assertThat;

class InheritanceQueryTest {

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
			.hasOption(String.class)
			.containsExactly(String.class, Serializable.class, Comparable.class, CharSequence.class, Object.class);
	}

	@Test
	void testStringOnlyInterfaces() {
		InheritanceQuery<String> chain = InheritanceQuery.of(String.class).onlyInterfaces();

		assertThat(chain)
			.hasOption(Serializable.class)
			.containsExactly(Serializable.class, Comparable.class, CharSequence.class);
	}

	@Test
	void testStringOnlyClasses() {
		InheritanceQuery<String> chain = InheritanceQuery.of(String.class).onlyClasses();

		assertThat(chain)
			.hasOption(String.class)
			.containsExactly(String.class, Object.class);
	}

	@Test
	void testFilter() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class)
			.filter(type -> type.getPackage().getName().startsWith("org.perfectable"));

		assertThat(chain)
			.hasOption(Leaf.class)
			.containsExactly(Leaf.class, Branch.class, Root.class);
	}

	@Test
	void testLeaf() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class);

		assertThat(chain)
			.hasOption(Leaf.class)
			.containsExactly(Leaf.class, Branch.class, Root.class, Object.class);
	}

	@Test
	void testExcludingRoot() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class).upToExcluding(Root.class);

		assertThat(chain)
			.hasOption(Leaf.class)
			.containsExactly(Leaf.class, Branch.class);
	}

	@Test
	void testIncludingRoot() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class).upToIncluding(Root.class);

		assertThat(chain)
			.hasOption(Leaf.class)
			.containsExactly(Leaf.class, Branch.class, Root.class);
	}
}
