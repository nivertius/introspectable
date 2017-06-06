package org.perfectable.introspection.injection;

import javax.inject.Inject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.perfectable.introspection.injection.Query.typed;

public class StandardRegistryTest {

	@Test
	public void testEmpty() {
		StandardRegistry registry = StandardRegistry.create();
		registry.register(EmptyService.class);

		EmptyService service = registry.fetch(typed(EmptyService.class));
		assertThat(service)
				.isNotNull();
	}

	@Test
	public void testConstructorSimple() {
		StandardRegistry registry = StandardRegistry.create();
		registry.register(EmptyService.class);
		registry.register(ConstructorService.class);

		ConstructorService service = registry.fetch(typed(ConstructorService.class));

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
