package org.perfectable.introspection.query;

import java.io.Serializable;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"static-method", "unchecked"})
public class InheritanceQueryTest {

	private static class Root {
		// test class
	}

	private static class Branch extends Root {
		// test class
	}

	private static class Leaf extends Branch {
		// test class
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testString() {
		InheritanceQuery<String> chain = InheritanceQuery.of(String.class);

		assertThat(chain)
				.containsExactly(String.class, Serializable.class, Comparable.class, CharSequence.class, Object.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testStringOnlyInterfaces() {
		InheritanceQuery<String> chain = InheritanceQuery.of(String.class).onlyInterfaces();

		assertThat(chain)
				.containsExactly(Serializable.class, Comparable.class, CharSequence.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testStringOnlyClasses() {
		InheritanceQuery<String> chain = InheritanceQuery.of(String.class).onlyClasses();

		assertThat(chain)
				.containsExactly(String.class, Object.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testFilter() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class)
				.filter(c -> c.getPackage().getName().startsWith("org.perfectable"));

		assertThat(chain)
				.containsExactly(Leaf.class, Branch.class, Root.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLeaf() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class);

		assertThat(chain)
				.containsExactly(Leaf.class, Branch.class, Root.class, Object.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExcludingRoot() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class).upToExcluding(Root.class);

		assertThat(chain)
				.containsExactly(Leaf.class, Branch.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testIncludingRoot() {
		InheritanceQuery<Leaf> chain = InheritanceQuery.of(Leaf.class).upToIncluding(Root.class);

		assertThat(chain)
				.containsExactly(Leaf.class, Branch.class, Root.class);
	}
}
