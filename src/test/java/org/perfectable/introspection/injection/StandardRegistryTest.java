package org.perfectable.introspection.injection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.inject.Inject;
import javax.inject.Qualifier;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.perfectable.introspection.injection.Query.typed;
import static org.perfectable.introspection.injection.Registration.singleton;
import static org.perfectable.introspection.injection.Registration.type;

public class StandardRegistryTest {

	@Test
	public void testEmpty() {
		StandardRegistry registry = StandardRegistry.create();
		registry.register(type(EmptyService.class));

		EmptyService service = registry.fetch(typed(EmptyService.class));
		assertThat(service)
			.isNotNull();
	}

	@Test
	public void testConstructorCreated() {
		StandardRegistry registry = StandardRegistry.create();
		registry.register(type(EmptyService.class));
		registry.register(type(ConstructorService.class));

		ConstructorService service = registry.fetch(typed(ConstructorService.class));

		assertThat(service)
			.isNotNull();
		service.assertAnythingInjected();
	}

	@Test
	public void testConstructorProvided() {
		StandardRegistry registry = StandardRegistry.create();
		EmptyService emptyService = new EmptyService();
		registry.register(singleton(emptyService));
		registry.register(type(ConstructorService.class));

		ConstructorService service = registry.fetch(typed(ConstructorService.class));

		assertThat(service)
			.isNotNull();
		service.assertInjected(emptyService);
	}

	@Test
	public void testQualifiedQueryConstruction() {
		StandardRegistry registry = StandardRegistry.create();
		registry.register(type(EmptyService.class));
		registry.register(type(QualifiedService.class));

		EmptyService service =
			registry.fetch(typed(EmptyService.class).qualifiedWith(TestQualifier.class));

		assertThat(service)
			.isNotNull()
			.isInstanceOf(QualifiedService.class);
	}

	@Test
	public void testQualifiedQuerySingleton() {
		StandardRegistry registry = StandardRegistry.create();
		EmptyService unqualifiedEmptyService = new EmptyService();
		EmptyService qualifiedEmptyService = new QualifiedService();
		registry.register(singleton(unqualifiedEmptyService));
		registry.register(singleton(qualifiedEmptyService));

		EmptyService service =
			registry.fetch(typed(EmptyService.class).qualifiedWith(TestQualifier.class));

		assertThat(service)
			.isSameAs(qualifiedEmptyService);
	}

	@Test
	public void testExternalQualifiedQueryType() {
		StandardRegistry registry = StandardRegistry.create();
		EmptyService unqualifiedEmptyService = new EmptyService();
		EmptyService qualifiedEmptyService = new EmptyService();
		registry.register(singleton(unqualifiedEmptyService));
		registry.register(singleton(qualifiedEmptyService).with(TestQualifier.class));

		EmptyService service =
			registry.fetch(typed(EmptyService.class).qualifiedWith(TestQualifier.class));

		assertThat(service)
			.isSameAs(qualifiedEmptyService);
	}

	@Test
	public void testQualifiedSetterProvided() {
		StandardRegistry registry = StandardRegistry.create();
		EmptyService unqualifiedEmptyService = new EmptyService();
		EmptyService qualifiedEmptyService = new QualifiedService();
		registry.register(singleton(unqualifiedEmptyService));
		registry.register(singleton(qualifiedEmptyService));
		registry.register(type(QualifiedSetterService.class));

		QualifiedSetterService service =
			registry.fetch(typed(QualifiedSetterService.class));

		assertThat(service)
			.isNotNull();
		service.assertInjected(qualifiedEmptyService);
	}

	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	private @interface TestQualifier {
		// marker
	}

	private static class EmptyService {
		// test class
	}

	@TestQualifier
	private static class QualifiedService extends EmptyService {
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

		void assertInjected(EmptyService expected) {
			assertThat(dependency)
				.isSameAs(expected);
		}
	}

	private static class QualifiedSetterService {
		private EmptyService dependency;

		void assertAnythingInjected() {
			assertThat(dependency)
				.isNotNull();
		}

		void assertInjected(EmptyService expected) {
			assertThat(dependency)
				.isSameAs(expected);
		}

		@Inject
		void setDependency(@TestQualifier EmptyService dependency) {
			this.dependency = dependency;
		}
	}
}
