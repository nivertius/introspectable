package org.perfectable.introspection;

import org.perfectable.introspection.proxy.BoundInvocation;
import org.perfectable.introspection.proxy.InvocationHandler;
import org.perfectable.introspection.proxy.MethodBoundInvocation;
import org.perfectable.introspection.proxy.ProxyBuilder;
import org.perfectable.introspection.proxy.ProxyBuilderFactory;
import org.perfectable.introspection.proxy.ProxyBuilderFactory.Feature;

import java.lang.reflect.Method;
import javax.annotation.Nullable;

import com.google.common.base.Defaults;

import static com.google.common.base.Preconditions.checkState;

public final class ReferenceExtractor<T> {

	@FunctionalInterface
	public interface GenericMethodReference<T> {
		void execute(T self, Object... arguments);
	}

	@FunctionalInterface
	public interface NoArgumentMethodReference<T> {
		void execute(T self);
	}

	@FunctionalInterface
	public interface SingleArgumentMethodReference<T, A1> {
		void execute(T self, A1 argument1);
	}

	@FunctionalInterface
	public interface DoubleArgumentMethodReference<T, A1, A2> {
		void execute(T self, A1 argument1, A2 argument2);
	}

	private final ProxyBuilder<T> proxyBuilder;

	public static <T> ReferenceExtractor<T> of(Class<T> sourceClass) {
		ProxyBuilder<T> proxyBuilder = ProxyBuilderFactory.withFeature(Feature.SUPERCLASS).ofType(sourceClass);
		return new ReferenceExtractor<>(proxyBuilder);
	}

	public Method extractGeneric(GenericMethodReference<T> method) {
		ProcedureTestingHandler<T> handler = ProcedureTestingHandler.create();
		T proxy = this.proxyBuilder.instantiate(handler);
		method.execute(proxy);
		return handler.extract();
	}

	public Method extractNone(NoArgumentMethodReference<T> procedure) {
		GenericMethodReference<T> reference =
				(self, arguments) -> procedure.execute(self);
		return extractGeneric(reference);
	}

	public <A1> Method extractSingle(SingleArgumentMethodReference<? super T, A1> procedure) {
		GenericMethodReference<T> reference =
				(self, arguments) -> procedure.execute(self, null);
		return extractGeneric(reference);
	}

	public <A1, A2> Method extractDouble(DoubleArgumentMethodReference<T, A1, A2> procedure) {
		GenericMethodReference<T> reference =
				(self, arguments) -> procedure.execute(self, null, null);
		return extractGeneric(reference);
	}

	private ReferenceExtractor(ProxyBuilder<T> proxyBuilder) {
		this.proxyBuilder = proxyBuilder;
	}

	private static final class ProcedureTestingHandler<T> implements InvocationHandler<T> {
		@Nullable
		private Method executedMethod;

		public static <T> ProcedureTestingHandler<T> create() {
			return new ProcedureTestingHandler<>();
		}

		public Method extract() {
			return this.executedMethod;
		}

		@Override
		public Object handle(BoundInvocation<? extends T> invocation) throws Throwable {
			checkState(this.executedMethod == null);
			MethodBoundInvocation<? extends T> methodInvocation = (MethodBoundInvocation<? extends T>) invocation;
			MethodBoundInvocation.Decomposer<Method, T> decomposer = new MethodBoundInvocation.Decomposer<Method, T>() {
				private Method foundMethod;

				@Override
				public void method(Method method) {
					this.foundMethod = method;
				}

				@Override
				public void receiver(T receiver) {
					// ignored
				}

				@Override
				public <X> void argument(int index, Class<? super X> formal, X actual) {
					// ignored
				}

				@Override
				public Method finish() {
					return this.foundMethod;
				}
			};
			this.executedMethod = methodInvocation.decompose(decomposer);
			Class<?> expectedResultType = this.executedMethod.getReturnType();
			return Defaults.defaultValue(expectedResultType);
		}

	}
}
