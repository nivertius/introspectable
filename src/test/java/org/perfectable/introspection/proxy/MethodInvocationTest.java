package org.perfectable.introspection.proxy;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.perfectable.introspection.SimpleReflections.getMethod;

// SUPPRESS FILE MultipleStringLiterals
// SUPPRESS FILE MagicNumber
// SUPPRESS FILE IllegalThrows
class MethodInvocationTest {

	@Test
	void testInvokeNoArguments() throws Throwable {
		NoArguments instance = new NoArguments();

		MethodInvocation<NoArguments> invocation = MethodInvocation.of(NoArguments.METHOD, instance);

		Object result = invocation.invoke();

		assertThat(result).isNull();
		instance.assertExecuted();
	}

	@Test
	void testInvokeSimpleArguments() throws Throwable {
		SimpleArguments instance = new SimpleArguments();

		String firstArgument = "firstArgument";
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

		String firstArgument = "firstArgument";
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

		String firstArgument = "firstArgument";
		int secondArgument = 238;
		int thirdArgument = 474;
		MethodInvocation<VariablePrimitiveArguments> invocation =
			MethodInvocation.of(VariablePrimitiveArguments.METHOD, instance,
				firstArgument, secondArgument, thirdArgument);

		Object result = invocation.invoke();

		assertThat(result).isNull();
		instance.assertExecutedWith(firstArgument, new int[] {secondArgument, thirdArgument});
	}

	static class NoArguments {
		private static final Method METHOD = getMethod(NoArguments.class, "executeNoArgument");

		private boolean executed;

		void executeNoArgument() {
			assertThat(executed).isFalse();
			executed = true;
		}

		void assertExecuted() {
			assertThat(executed).isTrue();
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

		void executeVariable(String actualFirst, String... actualVariable) {
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

		private boolean executed;
		private String first;
		private int[] variable;

		void executePrimitive(String actualFirst, int... actualVariable) {
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
}
