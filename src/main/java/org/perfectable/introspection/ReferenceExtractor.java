package org.perfectable.introspection;

import org.perfectable.introspection.proxy.Invocation;
import org.perfectable.introspection.proxy.InvocationHandler;
import org.perfectable.introspection.proxy.ProxyBuilderFactory;

import java.lang.reflect.Method;
import javax.annotation.Nullable;

import com.google.common.base.Defaults;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

public final class ReferenceExtractor<X> {
	public static <X> ReferenceExtractor<X> of(Class<X> type) {
		return new ReferenceExtractor<>(type);
	}

	private final Class<X> type;

	private ReferenceExtractor(Class<X> type) {
		this.type = type;
	}

	@FunctionalInterface
	public interface FunctionReference<X, R> {
		@SuppressWarnings("UnusedReturnValue")
		@CanIgnoreReturnValue
		R extract(X receiver);
	}

	public Method extract(FunctionReference<X, ?> method) {
		ExtractingHandler<X> extractingHandler = new ExtractingHandler<>();
		X proxy = ProxyBuilderFactory.withFeature(ProxyBuilderFactory.Feature.SUPERCLASS).ofType(type)
			.instantiate(extractingHandler);
		method.extract(proxy);
		return extractingHandler.finish();
	}

	private static final class ExtractingHandler<X> implements InvocationHandler<X> {
		@Nullable
		private Method foundMethod;

		@Nullable
		@Override
		public Object handle(Invocation<X> invocation) throws Throwable {
			if (foundMethod != null) {
				throw new IllegalStateException("Multiple methods were called");
			}
			foundMethod = invocation.decompose((method, receiver, arguments) -> method);
			return Defaults.defaultValue(foundMethod.getReturnType());
		}

		@SuppressWarnings("ReturnMissingNullable")
		Method finish() {
			if (foundMethod == null) {
				throw new IllegalStateException("No method was called");
			}
			return foundMethod;
		}
	}

}
