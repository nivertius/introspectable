package org.perfectable.introspection.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class MethodInvocation<T> implements Invocation<T> {
	private final Method method;
	private final T receiver;
	private final Object[] arguments;

	public static <T> MethodInvocation<T> of(Method method, T receiver, Object... arguments) {
		return new MethodInvocation<>(method, receiver, arguments);
	}

	public MethodInvocation(Method method, T receiver, Object... arguments) {
		this.method = method;
		this.receiver = receiver;
		this.arguments = arguments;
	}

	@Override
	public Object invoke() throws Throwable {
		try {
			Object result = this.method.invoke(this.receiver, this.arguments);
			return result;
		}
		catch (InvocationTargetException e) {
			throw e.getCause();
		}
	}

	public <X extends T> MethodInvocation<X> replaceReceiver(X newReceiver) {
		return MethodInvocation.of(method, newReceiver, arguments);
	}

	// SUPPRESS NEXT IllegalThrows
	public Object proceed(Processor<? super T> processor) throws Throwable {
		return processor.process(method, receiver, arguments);
	}

	public <R> R decompose(Decomposer<? super T, R> decomposer) {
		return decomposer.decompose(method, receiver, arguments);
	}

	public interface Processor<T> {
		// SUPPRESS NEXT IllegalThrows
		Object process(Method method, T receiver, Object... arguments) throws Throwable;
	}

	public interface Decomposer<T, R> {
		R decompose(Method method, T receiver, Object... arguments);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.method, this.receiver, Arrays.hashCode(this.arguments));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof MethodInvocation<?>)) {
			return false;
		}
		MethodInvocation<?> other = (MethodInvocation<?>) obj;
		return Objects.equals(this.method, other.method)
				&& Objects.equals(this.receiver, other.receiver)
				&& Arrays.equals(this.arguments, other.arguments);
	}
}
