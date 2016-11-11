package org.perfectable.introspection;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("static-method")
public class InheritanceChainTest {

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
		InheritanceChain<String> chain = InheritanceChain.startingAt(String.class);

		assertThat(chain).containsExactly(String.class, Object.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testLeaf() {
		InheritanceChain<Leaf> chain = InheritanceChain.startingAt(Leaf.class);

		assertThat(chain).containsExactly(Leaf.class, Branch.class, Root.class, Object.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testExcludingRoot() {
		InheritanceChain<Leaf> chain = InheritanceChain.startingAt(Leaf.class).upToExcluding(Root.class);

		assertThat(chain).containsExactly(Leaf.class, Branch.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testIncludingRoot() {
		InheritanceChain<Leaf> chain = InheritanceChain.startingAt(Leaf.class).upToIncluding(Root.class);

		assertThat(chain).containsExactly(Leaf.class, Branch.class, Root.class);
	}
}
