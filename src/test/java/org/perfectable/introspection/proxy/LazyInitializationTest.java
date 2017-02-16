package org.perfectable.introspection.proxy;

import org.perfectable.introspection.MockitoExtension;
import org.perfectable.introspection.proxy.LazyInitialization.Initializer;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class LazyInitializationTest {

	@Mock
	private TestFirstInterface firstMock;

	@Test
	public void testInitialize() {
		TestInitializer<TestFirstInterface> initializer = new TestInitializer<>(this.firstMock);
		TestFirstInterface proxy = LazyInitialization.createProxy(TestFirstInterface.class, initializer);
		initializer.assertNotExecuted();
		this.firstMock.firstMethod();
		proxy.firstMethod();
		initializer.assertExecuted();
	}

	@Test
	public void testExtractInstance() {
		TestInitializer<TestFirstInterface> initializer = new TestInitializer<>(this.firstMock);
		TestFirstInterface proxy = LazyInitialization.createProxy(TestFirstInterface.class, initializer);
		initializer.assertNotExecuted();
		assertThat(proxy).isInstanceOf(LazyInitialization.Proxy.class);

		@SuppressWarnings("unchecked")
		Optional<TestFirstInterface> uninitializedOption = ((LazyInitialization.Proxy<TestFirstInterface>) proxy)
				.extractInstance();
		assertThat(uninitializedOption).isEmpty();

		this.firstMock.firstMethod();
		proxy.firstMethod();

		initializer.assertExecuted();

		@SuppressWarnings("unchecked")
		Optional<TestFirstInterface> initializedOption = ((LazyInitialization.Proxy<TestFirstInterface>) proxy)
				.extractInstance();
		assertThat(initializedOption).contains(this.firstMock);
	}

	private static final class TestInitializer<T> implements Initializer<T> {
		private final T target;
		private boolean wasExecuted; // = false

		TestInitializer(T target) {
			this.target = target;
		}

		@Override
		public T initialize() {
			this.wasExecuted = true;
			return this.target;
		}

		void assertNotExecuted() {
			assertThat(this.wasExecuted).isFalse();
		}

		void assertExecuted() {
			assertThat(this.wasExecuted).isTrue();
		}
	}

	interface TestFirstInterface {
		void firstMethod();
	}
}
