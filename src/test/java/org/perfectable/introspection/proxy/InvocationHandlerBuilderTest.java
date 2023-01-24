package org.perfectable.introspection.proxy;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings({"MultipleStringLiterals", "nullness:argument"})
public class InvocationHandlerBuilderTest {

	@Test
	void testProcedure1() {
		Marker<Subject, Void> marker = new Marker<>(null);
		Subject proxy = InvocationHandlerBuilder.<Subject>create()
			.bind(Subject::doStuff)
				.as(receiver -> marker.call(receiver))
			.instantiate(Subject.class);

		proxy.doStuff();
		marker.check(proxy);
	}

	@Test
	void testProcedure2() {
		Marker<Subject, Void> marker = new Marker<>(null);
		Subject proxy = InvocationHandlerBuilder.<Subject>create()
			.bind(Subject::setValue)
				.as((receiver, argument) -> marker.call(receiver, argument))
			.instantiate(Subject.class);

		String newValue = "testValue";
		proxy.setValue(newValue);
		marker.check(proxy, newValue);
	}

	@SuppressWarnings("UnnecessaryTypeArgument")
	@Test
	void testFunction1() {
		String result = "testValue";
		Marker<Subject, @Nullable String> marker = new Marker<>(result);

		Subject proxy = InvocationHandlerBuilder.<Subject>create()
			.<@Nullable String, RuntimeException>bind(Subject::getValue)
				.as(receiver -> marker.call(receiver))
			.instantiate(Subject.class);

		@Nullable String actual = proxy.getValue();
		assertThat(actual).isEqualTo(result);
		marker.check(proxy);
	}

	@Test
	void testFunction2() {
		String result = "testValue";
		Marker<Subject, @Nullable String> marker = new Marker<>(result);

		Subject proxy = InvocationHandlerBuilder.<Subject>create()
			.<@Nullable String, @Nullable String, RuntimeException>bind(Subject::replaceValue)
				.as((receiver, argument) -> marker.call(receiver, argument))
			.instantiate(Subject.class);

		String newValue = "newValue";
		@Nullable String actual = proxy.replaceValue(newValue);
		assertThat(actual).isEqualTo(result);
		marker.check(proxy, newValue);
	}

	@SuppressWarnings("UnnecessaryTypeArgument")
	@Test
	void testObjectMethod() {
		String result = "testValue";
		Marker<Subject, String> marker = new Marker<>(result);

		Subject proxy = InvocationHandlerBuilder.<Subject>create()
			.bind(Object::toString)
				.<String, RuntimeException>as(receiver -> marker.call(receiver))
			.instantiate(Subject.class);

		@Nullable String actual = proxy.toString();
		assertThat(actual).isEqualTo(result);
		marker.check(proxy);
	}

	@SuppressWarnings("nullness:initialization.fields.uninitialized")
	private static final class Marker<T, V> {
		private boolean called;
		private T acceptedReceiver;
		private Object[] acceptedArguments;
		private final V result;

		private Marker(V result) {
			this.result = result;
		}

		@SuppressWarnings("AnnotationLocation")
		V call(T receiver, Object... arguments) {
			if (called) {
				throw new AssertionError("Marker was already called");
			}
			this.acceptedReceiver = receiver;
			this.acceptedArguments = arguments;
			this.called = true;
			return result;
		}

		void check(T expectedReceiver, Object... expectedArguments) {
			if (!called) {
				throw new AssertionError("Marker was not called");
			}
			assertThat(acceptedReceiver).isSameAs(expectedReceiver);
			assertThat(acceptedArguments).isEqualTo(expectedArguments);
		}
	}

	@SuppressWarnings("AnnotationLocation")
	private interface Subject {
		void doStuff();

		@Nullable String getValue();

		void setValue(@Nullable String value);

		@Nullable String replaceValue(@Nullable String value);
	}

}
