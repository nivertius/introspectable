package org.perfectable.introspection.injection;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class StandardRegistryTest {

	@Test
	public void testEmpty() {
		StandardRegistry registry = StandardRegistry.create();

		EmptyService service = registry.fetch(EmptyService.class);
		assertThat(service)
				.isNotNull();
	}

	@Test
	public void testConstructorSimple() {
		StandardRegistry registry = StandardRegistry.create();

		ConstructorService service = registry.fetch(ConstructorService.class);

		assertThat(service)
				.isNotNull();
		service.assertAnythingInjected();
	}

	private static class EmptyService {
		// test class
	}

	private static class ConstructorService {
		private final EmptyService dependency;

		@Inject
		ConstructorService(EmptyService dependency) {
			this.dependency = dependency;
		}

		void assertAnythingInjected() {
			assertThat(dependency)
					.isNotNull();
		}
	}
}
