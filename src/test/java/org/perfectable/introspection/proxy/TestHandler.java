package org.perfectable.introspection.proxy;

import org.perfectable.introspection.ObjectMethods;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.annotation.Nullable;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHandler<T> implements InvocationHandler<T> {
	private final Queue<Expectance> expected = new ArrayDeque<>();

	static <T> TestHandler<T> create() {
		return new TestHandler<>();
	}

	@Nullable
	@Override
	public Object handle(Invocation<T> invocation) throws Throwable {
		return invocation.proceed((method, receiver, arguments) -> {
			if (ObjectMethods.EQUALS.equals(method)) {
				return receiver == arguments[0];
			}
			if (ObjectMethods.HASH_CODE.equals(method)) {
				return System.identityHashCode(receiver);
			}
			if (ObjectMethods.TO_STRING.equals(method)) {
				return receiver.getClass() + "@" + System.identityHashCode(receiver);
			}
			Expectance expectance = expected.remove();
			return invocation.proceed(expectance);
		});
	}

	public Expectance expectInvocation(T proxy, Method method, Object... arguments) {
		Expectance expectance = new Expectance(proxy, method, arguments);
		expected.add(expectance);
		return expectance;
	}

	public void verify() {
		assertThat(expected)
			.as("Expectations")
			.isEmpty();
	}

	@FunctionalInterface
	interface InvocationResult {
		@Nullable
		Object resolve() throws Throwable; // SUPPRESS IllegalThrows
	}

	private static class ReturningResult implements InvocationResult {
		@Nullable
		private final Object value;

		ReturningResult(@Nullable Object value) {
			this.value = value;
		}

		@Nullable
		@Override
		public Object resolve() throws Throwable {
			return value;
		}
	}

	private static class ThrowingResult implements InvocationResult {
		private final Throwable thrown;

		ThrowingResult(Throwable thrown) {
			this.thrown = thrown;
		}

		@Nullable
		@Override
		public Object resolve() throws Throwable {
			throw thrown;
		}
	}

	class Expectance implements Invocation.Invoker<T> {
		private final T expectedProxy;
		private final Method expectedMethod;
		private final Object[] expectedArguments;

		private InvocationResult result =
			() -> new AssertionError("Result has not been set");

		Expectance(T expectedProxy, Method expectedMethod, Object... expectedArguments) {
			this.expectedProxy = expectedProxy;
			this.expectedMethod = expectedMethod;
			this.expectedArguments = expectedArguments.clone();
		}

		public void andReturn(@Nullable Object value) {
			this.result = new ReturningResult(value);
		}

		public void andThrow(Throwable thrown) {
			this.result = new ThrowingResult(thrown);
		}

		@Nullable
		@Override
		public Object process(Method method, @Nullable T receiver, Object... arguments) throws Throwable {
			assertThat(receiver).isEqualTo(expectedProxy);
			assertThat(method).isEqualTo(expectedMethod);
			assertThat(arguments).isEqualTo(expectedArguments);
			return result.resolve();
		}
	}

}
