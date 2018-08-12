package org.perfectable.introspection.proxy;

import org.perfectable.introspection.ObjectMethods;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Queue;
import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

public class TestHandler<T> implements InvocationHandler<T> {
	private final Queue<Expectance> expected = new ArrayDeque<>();

	static <T> TestHandler<T> create() {
		return new TestHandler<>();
	}

	@Nullable
	@Override
	public Object handle(Invocation<T> invocation) throws Throwable {
		Invocation.Decomposer<T, Invocation<? super T>> trDecomposer = (method, receiver, arguments) -> {
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
			InvocationResult result = expectance.process(method, receiver, arguments);
			return new ResultInvocation(result, method, receiver, arguments);
		};
		return invocation.decompose(trDecomposer).invoke();
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

	class Expectance {
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

		public InvocationResult process(Method method, @Nullable T receiver, Object... arguments) {
			assertThat(receiver).isEqualTo(expectedProxy);
			assertThat(method).isEqualTo(expectedMethod);
			assertThat(arguments).isEqualTo(expectedArguments);
			return result;
		}
	}

	private static class EqualsInvocation implements Invocation<Object> {
		private final Object receiver;
		private final Object other;

		EqualsInvocation(Object receiver, Object other) {
			this.receiver = receiver;
			this.other = other;
		}

		@Override
		public Object invoke() throws Throwable {
			return receiver == other; // SUPPRESS CompareObjectsWithEquals
		}

		@Override
		public <R> R decompose(Decomposer<? super Object, R> decomposer) {
			return decomposer.decompose(ObjectMethods.EQUALS, receiver, other);
		}
	}

	private static class HashCodeInvocation implements Invocation<Object> {
		private final Object receiver;

		HashCodeInvocation(Object receiver) {
			this.receiver = receiver;
		}

		@Nullable
		@Override
		public Object invoke() throws Throwable {
			return System.identityHashCode(receiver);
		}

		@Override
		public <R> R decompose(Decomposer<? super Object, R> decomposer) {
			return decomposer.decompose(ObjectMethods.HASH_CODE, receiver);
		}
	}

	private static class ToStringInvocation implements Invocation<Object> {
		private final Object receiver;

		ToStringInvocation(Object receiver) {
			this.receiver = receiver;
		}

		@Nullable
		@Override
		public Object invoke() throws Throwable {
			return receiver.getClass() + "@" + System.identityHashCode(receiver);
		}

		@Override
		public <R> R decompose(Decomposer<? super Object, R> decomposer) {
			return decomposer.decompose(ObjectMethods.TO_STRING, receiver);
		}
	}

	private static class ResultInvocation implements Invocation<Object> {
		private final InvocationResult result;
		private final Method method;
		private final Object receiver;
		private final Object[] arguments;

		ResultInvocation(InvocationResult result, Method method, Object receiver,
						 Object... arguments) { // SUPPRESS ArrayIsStoredDirectly
			this.result = result;
			this.method = method;
			this.receiver = receiver;
			this.arguments = arguments;
		}

		@Nullable
		@Override
		public Object invoke() throws Throwable {
			return result.resolve();
		}

		@Override
		public <R> R decompose(Decomposer<? super Object, R> decomposer) {
			return decomposer.decompose(method, receiver, arguments);
		}
	}
}
