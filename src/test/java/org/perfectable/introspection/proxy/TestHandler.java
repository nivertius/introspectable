package org.perfectable.introspection.proxy;

import org.perfectable.introspection.ObjectMethods;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

public class TestHandler<T> implements InvocationHandler<MethodInvocation<T>> {
	private final Queue<Expectance> expected = new ArrayDeque<>();

	static <T> TestHandler<T> create() {
		return new TestHandler<>();
	}

	@Nullable
	@Override
	public Object handle(MethodInvocation<T> invocation) throws Throwable {
		return invocation.decompose(this::replaceInvocation).invoke();
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

	private Invocation replaceInvocation(Method method, T receiver, Object... arguments) {
		requireNonNull(receiver);
		if (ObjectMethods.EQUALS.equals(method)) {
			return new EqualsInvocation(receiver, arguments[0]);
		}
		if (ObjectMethods.HASH_CODE.equals(method)) {
			return new HashCodeInvocation(receiver);
		}
		if (ObjectMethods.TO_STRING.equals(method)) {
			return new ToStringInvocation(receiver);
		}
		Expectance expectance = expected.remove();
		Invocation result = expectance.process(method, receiver, arguments);
		return result;
	}

	@FunctionalInterface
	interface InvocationResult {
		@Nullable
		Object resolve() throws Throwable; // SUPPRESS IllegalThrows
	}

	class Expectance {
		private final T expectedProxy;
		private final Method expectedMethod;
		private final Object[] expectedArguments;

		private Invocation result =
			() -> new AssertionError("Result has not been set");

		Expectance(T expectedProxy, Method expectedMethod, Object... expectedArguments) {
			this.expectedProxy = expectedProxy;
			this.expectedMethod = expectedMethod;
			this.expectedArguments = expectedArguments.clone();
		}

		public void andReturn(@Nullable Object value) {
			this.result = Invocation.returning(value);
		}

		public void andThrow(Throwable thrown) {
			this.result = Invocation.throwing(thrown);
		}

		public Invocation process(Method method, @Nullable T receiver, Object... arguments) {
			assertThat(receiver).isEqualTo(expectedProxy);
			assertThat(method).isEqualTo(expectedMethod);
			assertThat(arguments).isEqualTo(expectedArguments);
			return result;
		}
	}

	private static class EqualsInvocation implements Invocation {
		private final Object receiver;
		private final Object other;

		EqualsInvocation(Object receiver, Object other) {
			this.receiver = receiver;
			this.other = other;
		}

		@Override
		public Object invoke() {
			return receiver == other; // SUPPRESS CompareObjectsWithEquals
		}
	}

	private static class HashCodeInvocation implements Invocation {
		private final Object receiver;

		HashCodeInvocation(Object receiver) {
			this.receiver = receiver;
		}

		@Nullable
		@Override
		public Object invoke() {
			return System.identityHashCode(receiver);
		}
	}

	private static class ToStringInvocation implements Invocation {
		private final Object receiver;

		ToStringInvocation(Object receiver) {
			this.receiver = receiver;
		}

		@Nullable
		@Override
		public Object invoke() {
			return receiver.getClass() + "@" + System.identityHashCode(receiver);
		}
	}
}
