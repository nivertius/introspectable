package org.perfectable.introspection.proxy; // SUPPRESS LENGTH

import java.lang.reflect.Method;

import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
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
		assertThatThrownBy(() -> MethodInvocation.of(NoArguments.METHOD1, null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Method " + NoArguments.METHOD1
				+ " is not static, got null as receiver");
	}

	@Test
	void testNegativeCallabilityInvalidReceiverType() throws Throwable {
		VariableArguments instance = new VariableArguments();
		assertThatThrownBy(() -> MethodInvocation.of(NoArguments.METHOD1, instance))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Method " + NoArguments.METHOD1
				+ " requires " + NoArguments.class + " as receiver, got " + instance);
	}

	@Test
	void testNegativeCallabilityInvalidArgumentCountConstantArguments() throws Throwable {
		NoArguments instance = new NoArguments();
		assertThatThrownBy(() -> MethodInvocation.of(NoArguments.METHOD1, instance, 1))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Method " + NoArguments.METHOD1
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

		MethodInvocation<NoArguments> invocation = MethodInvocation.of(NoArguments.METHOD1, instance);

		@Nullable Object result = invocation.invoke();

		assertThat(result).isNull();
		instance.assertExecuted1();
	}

	@Test
	void testInvokeNoArgumentsReceiverReplaced() throws Throwable {
		NoArguments instance = new NoArguments();
		NoArguments replaced = new NoArguments();

		MethodInvocation<NoArguments> invocation =
			MethodInvocation.of(NoArguments.METHOD1, instance).withReceiver(replaced);

		@Nullable Object result = invocation.invoke();

		assertThat(result).isNull();
		instance.assertNotExecuted();
		replaced.assertExecuted1();
	}

	@Test
	void testInvokeNoArgumentsMethodReplaced() throws Throwable {
		NoArguments instance = new NoArguments();

		MethodInvocation<NoArguments> invocation =
			MethodInvocation.of(NoArguments.METHOD1, instance).withMethod(NoArguments.METHOD2);

		@Nullable Object result = invocation.invoke();

		assertThat(result).isNull();
		instance.assertExecuted2();
	}

	@Test
	void testInvokeSimpleArgumentsReplaced() throws Throwable {
		SimpleArguments instance = new SimpleArguments();

		String firstArgument = EXAMPLE_FIRST_ARGUMENT;
		String secondArgument = "secondArgument";
		String replacedFirstArgument = "replacedFirstArgument";
		String replacedSecondArgument = "replacedSecondArgument";
		MethodInvocation<SimpleArguments> invocation =
			MethodInvocation.of(SimpleArguments.METHOD, instance, firstArgument, secondArgument);
		MethodInvocation<SimpleArguments> replaced =
			invocation.withArguments(replacedFirstArgument, replacedSecondArgument);

		@Nullable Object result = replaced.invoke();

		assertThat(result).isNull();
		instance.assertExecutedWith(replacedFirstArgument, replacedSecondArgument);
	}

	@Test
	void testInvokeSimpleArguments() throws Throwable {
		SimpleArguments instance = new SimpleArguments();

		String firstArgument = EXAMPLE_FIRST_ARGUMENT;
		String secondArgument = "secondArgument";
		MethodInvocation<SimpleArguments> invocation =
			MethodInvocation.of(SimpleArguments.METHOD, instance, firstArgument, secondArgument);

		@Nullable Object result = invocation.invoke();

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

		@Nullable Object result = invocation.invoke();

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

		@Nullable Object result = invocation.invoke();

		assertThat(result).isNull();
		instance.assertExecutedWith(firstArgument, new int[] {secondArgument, thirdArgument});
	}

	@SuppressWarnings("argument.type.incompatible")
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
	void testDecomposition() {
		VariablePrimitiveArguments instance = new VariablePrimitiveArguments();

		String firstArgument = EXAMPLE_FIRST_ARGUMENT;
		int secondArgument = 238;
		int thirdArgument = 474;
		MethodInvocation<VariablePrimitiveArguments> invocation =
			MethodInvocation.of(VariablePrimitiveArguments.METHOD, instance,
				firstArgument, secondArgument, thirdArgument);


		MethodInvocation.Decomposer<Object, Decomposition> decomposer = Decomposition::new;
		Decomposition result = invocation.decompose(decomposer);
		result.assertMethod(VariablePrimitiveArguments.METHOD);
		result.assertReceiver(instance);
		result.assertArguments(firstArgument, secondArgument, thirdArgument);
	}


	static class NoArguments {
		private static final Method METHOD1 = getMethod(NoArguments.class, "executeNoArgument1");
		private static final Method METHOD2 = getMethod(NoArguments.class, "executeNoArgument2");
		static final Method METHOD_STATIC = getMethod(NoArguments.class, "stubStatic");

		private boolean executed1;
		private boolean executed2;

		void executeNoArgument1() {
			assertThat(executed1).isFalse();
			executed1 = true;
		}

		void executeNoArgument2() {
			assertThat(executed2).isFalse();
			executed2 = true;
		}

		static void stubStatic() {
			throw new AssertionError("Stub method actually called");
		}

		void assertExecuted1() {
			assertThat(executed1).isTrue();
		}

		void assertExecuted2() {
			assertThat(executed2).isTrue();
		}

		void assertNotExecuted() {
			assertThat(executed1).isFalse();
			assertThat(executed2).isFalse();
		}
	}

	static class SimpleArguments {
		private static final Method METHOD =
			getMethod(SimpleArguments.class, "executeSimple", String.class, String.class);

		private boolean executed;
		private @MonotonicNonNull String first;
		private @MonotonicNonNull String second;


		void executeSimple(String actualFirst, String actualSecond) {
			assertThat(executed).isFalse();
			executed = true;
			first = actualFirst;
			second = actualSecond;
		}

		@SuppressWarnings("argument.type.incompatible")
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
		private @MonotonicNonNull String first;
		private String @MonotonicNonNull [] variable;

		void executeVariable(String actualFirst, String... actualVariable) { // SUPPRESS ArrayIsStoredDirectly
			assertThat(executed).isFalse();
			executed = true;
			first = actualFirst;
			variable = actualVariable;
		}

		@SuppressWarnings("argument.type.incompatible")
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
		private @MonotonicNonNull String first;
		private int @MonotonicNonNull [] variable;

		void executePrimitive(String actualFirst, int... actualVariable) { // SUPPRESS ArrayIsStoredDirectly
			assertThat(executed).isFalse();
			executed = true;
			first = actualFirst;
			variable = actualVariable;
		}

		@SuppressWarnings("argument.type.incompatible")
		void assertExecutedWith(String expectedFirst, int[] expectedVariable) { // SUPPRESS UseVarargs
			assertThat(executed).isTrue();
			assertThat(first).isEqualTo(expectedFirst);
			assertThat(variable).isEqualTo(expectedVariable);
		}
	}

	private static final class Decomposition {
		private final Method method;
		private final @Nullable Object receiver;
		private final @Nullable Object[] arguments;

		Decomposition(Method method, @Nullable Object receiver, @Nullable Object[] arguments) { // SUPPRESS UseVarargs
			this.method = method;
			this.receiver = receiver;
			this.arguments = arguments;
		}

		public void assertMethod(Method expectedMethod) {
			assertThat(this.method).isEqualTo(expectedMethod);
		}

		@SuppressWarnings("argument.type.incompatible")
		public void assertReceiver(@Nullable Object expectedReceiver) {
			assertThat(this.receiver).isEqualTo(expectedReceiver);
		}


		public void assertArguments(@Nullable Object... expectedArguments) {
			assertThat(this.arguments).isEqualTo(expectedArguments);
		}
	}
}
