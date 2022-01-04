package org.perfectable.introspection.proxy;

import org.perfectable.introspection.ObjectMethods;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Queue;

import org.checkerframework.checker.nullness.qual.Nullable;

import static org.assertj.core.api.Assertions.assertThat;

public class TestHandler<T> implements InvocationHandler<@Nullable Object, Exception, MethodInvocation<T>> {
	private final Queue<Expectance> expected = new ArrayDeque<>();

	static <T> TestHandler<T> create() {
		return new TestHandler<>();
	}

	@Override
	public @Nullable Object handle(MethodInvocation<T> invocation) throws Exception {
		return invocation.decompose(this::replaceInvocation).invoke();
	}

	public Expectance expectInvocation(@Nullable T proxy, Method method, @Nullable Object... arguments) {
		Expectance expectance = new Expectance(proxy, method, arguments);
		expected.add(expectance);
		return expectance;
	}

	public void verify() {
		assertThat(expected)
			.as("Expectations")
			.isEmpty();
	}

	private Invocation<?, ?> replaceInvocation(Method method, @Nullable T receiver, @Nullable Object... arguments) {
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
		return expectance.process(method, receiver, arguments);
	}

	class Expectance {
		private final @Nullable T expectedProxy;
		private final Method expectedMethod;
		private final @Nullable Object[] expectedArguments;

		private Invocation<?, ?> result;

		Expectance(@Nullable T expectedProxy, Method expectedMethod, @Nullable Object... expectedArguments) {
			this.expectedProxy = expectedProxy;
			this.expectedMethod = expectedMethod;
			this.expectedArguments = expectedArguments.clone();
			result = () -> {
				throw new AssertionError("Result has not been set");
			};
		}

		public void andReturn(@Nullable Object value) {
			this.result = Invocation.returning(value);
		}

		public void andThrow(Exception thrown) {
			this.result = Invocation.throwing(() -> thrown);
		}

		@SuppressWarnings("argument.type.incompatible")
		public Invocation<?, ?> process(Method method, @Nullable T receiver, @Nullable Object... arguments) {
			assertThat(receiver).isEqualTo(expectedProxy);
			assertThat(method).isEqualTo(expectedMethod);
			assertThat(arguments).isEqualTo(expectedArguments);
			return result;
		}
	}

	private static class EqualsInvocation implements Invocation<Boolean, RuntimeException> {
		private final @Nullable Object receiver;
		private final @Nullable Object other;

		EqualsInvocation(@Nullable Object receiver, @Nullable Object other) {
			this.receiver = receiver;
			this.other = other;
		}

		@Override
		public Boolean invoke() {
			return receiver == other; // SUPPRESS CompareObjectsWithEquals
		}
	}

	private static class HashCodeInvocation implements Invocation<Integer, RuntimeException> {
		private final @Nullable Object receiver;

		HashCodeInvocation(@Nullable Object receiver) {
			this.receiver = receiver;
		}

		@Override
		public Integer invoke() {
			return System.identityHashCode(receiver);
		}
	}

	private static class ToStringInvocation implements Invocation<String, RuntimeException> {
		private final @Nullable Object receiver;

		ToStringInvocation(@Nullable Object receiver) {
			this.receiver = receiver;
		}

		@Override
		public String invoke() {
			if (receiver == null) {
				return "null";
			}
			return receiver.getClass() + "@" + System.identityHashCode(receiver);
		}
	}
}
