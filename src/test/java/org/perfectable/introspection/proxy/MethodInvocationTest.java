package org.perfectable.introspection.proxy; // SUPPRESS LENGTH

import java.lang.reflect.Method;
import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.perfectable.introspection.SimpleReflections.getMethod;

// SUPPRESS FILE MultipleStringLiterals
// SUPPRESS FILE MagicNumber
// SUPPRESS FILE IllegalThrows
class MethodInvocationTest {

	private static final String EXAMPLE_FIRST_ARGUMENT = "firstArgument";

	@Test
	void testNegativeCallabilityStaticNonNullReceiver() throws Throwable {
		NoArguments instance = new NoArguments();

		assertThatThrownBy(() -> MethodInvocation.of(NoArguments.METHOD_STATIC, instance))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Method " + NoArguments.METHOD_STATIC // SUPPRESS AvoidDuplicateLiterals
				+ " is static, got " + instance + " as receiver");
	}

	@Test
	void testNegativeCallabilityNonStaticNullReceiver() throws Throwable {
		assertThatThrownBy(() -> MethodInvocation.of(NoArguments.METHOD, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Method " + NoArguments.METHOD
				+ " is not static, got null as receiver");
	}

	@Test
	void testNegativeCallabilityInvalidReceiverType() throws Throwable {
		VariableArguments instance = new VariableArguments();
		assertThatThrownBy(() -> MethodInvocation.of(NoArguments.METHOD, instance))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Method " + NoArguments.METHOD
				+ " requires " + NoArguments.class + " as receiver, got " + instance);
	}

	@Test
	void testNegativeCallabilityInvalidArgumentCountConstantArguments() throws Throwable {
		NoArguments instance = new NoArguments();
		assertThatThrownBy(() -> MethodInvocation.of(NoArguments.METHOD, instance, 1))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Method " + NoArguments.METHOD
				+ " requires 0 arguments, got 1");
	}

	@Test
	void testNegativeCallabilityInvalidArgumentCountVariableArguments() throws Throwable {
		VariableArguments instance = new VariableArguments();
		assertThatThrownBy(() -> MethodInvocation.of(VariableArguments.METHOD, instance))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Method " + VariableArguments.METHOD
				+ " requires at least 1 arguments, got 0");
	}

	@Test
	void testNegativeCallabilityNullPrimitiveArgument() throws Throwable {
		String firstArgument = EXAMPLE_FIRST_ARGUMENT;
		VariablePrimitiveArguments instance = new VariablePrimitiveArguments();
		assertThatThrownBy(() -> MethodInvocation.of(VariablePrimitiveArguments.METHOD, instance, firstArgument, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Method " + VariablePrimitiveArguments.METHOD
				+ " has primitive int as parameter 2, got null argument");
	}

	@Test
	void testNegativeCallabilityInvalidArgumentType() throws Throwable {
		String firstArgument = EXAMPLE_FIRST_ARGUMENT;
		int secondArgument = 329387;
		VariableArguments instance = new VariableArguments();
		assertThatThrownBy(() -> MethodInvocation.of(VariableArguments.METHOD, instance,
			firstArgument, secondArgument))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Method " + VariableArguments.METHOD + " takes " + String.class + " as parameter 2,"
				+ " got " + secondArgument + " as argument");
	}

	@Test
	void testInvokeNoArguments() throws Throwable {
		NoArguments instance = new NoArguments();

		MethodInvocation<NoArguments> invocation = MethodInvocation.of(NoArguments.METHOD, instance);

		Object result = invocation.invoke();

		assertThat(result).isNull();
		instance.assertExecuted();
	}

	@Test
	void testInvokeNoArgumentsReplaced() throws Throwable {
		NoArguments instance = new NoArguments();
		NoArguments replaced = new NoArguments();

		Invocation<NoArguments> invocation =
			MethodInvocation.of(NoArguments.METHOD, instance).withReceiver(replaced);

		Object result = invocation.invoke();

		assertThat(result).isNull();
		instance.assertNotExecuted();
		replaced.assertExecuted();
	}

	@Test
	void testInvokeSimpleArguments() throws Throwable {
		SimpleArguments instance = new SimpleArguments();

		String firstArgument = EXAMPLE_FIRST_ARGUMENT;
		String secondArgument = "secondArgument";
		MethodInvocation<SimpleArguments> invocation =
			MethodInvocation.of(SimpleArguments.METHOD, instance, firstArgument, secondArgument);

		Object result = invocation.invoke();

		assertThat(result).isNull();
		instance.assertExecutedWith(firstArgument, secondArgument);
	}

	@Test
	void testInvokeVariableArguments() throws Throwable {
		VariableArguments instance = new VariableArguments();

		String firstArgument = EXAMPLE_FIRST_ARGUMENT;
		String secondArgument = "secondArgument";
		String thirdArgument = "thirdArgument";
		MethodInvocation<VariableArguments> invocation =
			MethodInvocation.of(VariableArguments.METHOD, instance, firstArgument, secondArgument, thirdArgument);

		Object result = invocation.invoke();

		assertThat(result).isNull();
		instance.assertExecutedWith(firstArgument, new String[] { secondArgument, thirdArgument });
	}

	@Test
	void testInvokeVariablePrimitiveArguments() throws Throwable {
		VariablePrimitiveArguments instance = new VariablePrimitiveArguments();

		String firstArgument = EXAMPLE_FIRST_ARGUMENT;
		int secondArgument = 238;
		int thirdArgument = 474;
		MethodInvocation<VariablePrimitiveArguments> invocation =
			MethodInvocation.of(VariablePrimitiveArguments.METHOD, instance,
				firstArgument, secondArgument, thirdArgument);

		Object result = invocation.invoke();

		assertThat(result).isNull();
		instance.assertExecutedWith(firstArgument, new int[] {secondArgument, thirdArgument});
	}

	@Test
	void testEquality() {
		VariablePrimitiveArguments instance = new VariablePrimitiveArguments();
		VariablePrimitiveArguments otherInstance = new VariablePrimitiveArguments();

		String firstArgument = EXAMPLE_FIRST_ARGUMENT;
		int secondArgument = 238;
		int thirdArgument = 474;
		MethodInvocation<VariablePrimitiveArguments> invocation =
			MethodInvocation.of(VariablePrimitiveArguments.METHOD, instance,
				firstArgument, secondArgument, thirdArgument);

		assertThat(invocation).isEqualTo(invocation);
		assertThat(invocation).isEqualTo(
			MethodInvocation.of(VariablePrimitiveArguments.METHOD, instance,
				firstArgument, secondArgument, thirdArgument));
		assertThat(invocation).isNotEqualTo(
			MethodInvocation.of(VariablePrimitiveArguments.EXPECT_METHOD, instance,
				firstArgument, new int[] { secondArgument, thirdArgument }));
		assertThat(invocation).isNotEqualTo(
			MethodInvocation.of(VariablePrimitiveArguments.METHOD, otherInstance,
				firstArgument, secondArgument, thirdArgument));
		assertThat(invocation).isNotEqualTo(
			MethodInvocation.of(VariablePrimitiveArguments.METHOD, instance,
				firstArgument, secondArgument));
		assertThat(invocation).isNotEqualTo(null);
		assertThat(invocation).isNotEqualTo(new Object());
	}

	@Test
	void testWrapInto() throws Throwable {
		NoArguments instance = new NoArguments();

		MethodInvocation<NoArguments> invocation = MethodInvocation.of(NoArguments.METHOD, instance);

		Object expectedResult = new Object();
		TestInvocationHandler<NoArguments> handler = new TestInvocationHandler<>(expectedResult);
		Invocation<NoArguments> wrapped = invocation.wrapInto(handler);

		assertThat(wrapped).isNotNull();
		instance.assertNotExecuted();
		handler.assertNotExecuted();

		Object result = wrapped.invoke();
		instance.assertNotExecuted(); // test handler doesn't actually execute invocation
		handler.assertExecutedWith(invocation);
		assertThat(result).isSameAs(expectedResult);
	}

	static class NoArguments {
		private static final Method METHOD = getMethod(NoArguments.class, "executeNoArgument");
		static final Method METHOD_STATIC = getMethod(NoArguments.class, "stubStatic");

		private boolean executed;

		void executeNoArgument() {
			assertThat(executed).isFalse();
			executed = true;
		}

		static void stubStatic() {
			throw new AssertionError("Stub method actually called");
		}

		void assertExecuted() {
			assertThat(executed).isTrue();
		}

		void assertNotExecuted() {
			assertThat(executed).isFalse();
		}
	}

	static class SimpleArguments {
		private static final Method METHOD =
			getMethod(SimpleArguments.class, "executeSimple", String.class, String.class);

		private boolean executed;
		private String first;
		private String second;


		void executeSimple(String actualFirst, String actualSecond) {
			assertThat(executed).isFalse();
			executed = true;
			first = actualFirst;
			second = actualSecond;
		}

		void assertExecutedWith(String expectedFirst, String expectedSecond) {
			assertThat(executed).isTrue();
			assertThat(first).isEqualTo(expectedFirst);
			assertThat(second).isEqualTo(expectedSecond);
		}
	}


	static class VariableArguments {
		private static final Method METHOD =
			getMethod(VariableArguments.class, "executeVariable", String.class, String[].class);

		private boolean executed;
		private String first;
		private String[] variable;

		void executeVariable(String actualFirst, String... actualVariable) { // SUPPRESS ArrayIsStoredDirectly
			assertThat(executed).isFalse();
			executed = true;
			first = actualFirst;
			variable = actualVariable;
		}

		void assertExecutedWith(String expectedFirst, String[] expectedVariable) { // SUPPRESS UseVarargs
			assertThat(executed).isTrue();
			assertThat(first).isEqualTo(expectedFirst);
			assertThat(variable).isEqualTo(expectedVariable);
		}
	}

	static class VariablePrimitiveArguments {
		private static final Method METHOD =
			getMethod(VariablePrimitiveArguments.class, "executePrimitive", String.class, int[].class);
		public static final Method EXPECT_METHOD =
			getMethod(VariablePrimitiveArguments.class, "assertExecutedWith", String.class, int[].class);

		private boolean executed;
		private String first;
		private int[] variable;

		void executePrimitive(String actualFirst, int... actualVariable) { // SUPPRESS ArrayIsStoredDirectly
			assertThat(executed).isFalse();
			executed = true;
			first = actualFirst;
			variable = actualVariable;
		}

		void assertExecutedWith(String expectedFirst, int[] expectedVariable) { // SUPPRESS UseVarargs
			assertThat(executed).isTrue();
			assertThat(first).isEqualTo(expectedFirst);
			assertThat(variable).isEqualTo(expectedVariable);
		}
	}

	private static final class TestInvocationHandler<T> implements InvocationHandler<T> {
		private final Object result;

		private boolean executed;
		private Invocation<T> executedInvocation;

		TestInvocationHandler(Object result) {
			this.result = result;
		}

		@Nullable
		@Override
		public Object handle(Invocation<T> invocation) throws Throwable {
			assertNotExecuted();
			executed = true;
			executedInvocation = invocation;
			return result;
		}

		void assertNotExecuted() {
			assertThat(executed).isFalse();
		}

		void assertExecutedWith(Invocation<T> expectedInvocation) {
			assertThat(executed).isTrue();
			assertThat(executedInvocation).isSameAs(expectedInvocation);
		}
	}
}
