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

// SUPPRESS NEXT MethodCount
public final class ReferenceExtractor<T> {

	@FunctionalInterface
	private interface MethodReference<T> {
		void execute(T self, Object... arguments);
	}

	@FunctionalInterface
	public interface ProcedureNone<T> {
		void execute(T self);
	}

	@FunctionalInterface
	public interface ProcedureSingle<T, A1> {
		void execute(T self, A1 argument1);
	}

	@FunctionalInterface
	public interface ProcedureDouble<T, A1, A2> {
		void execute(T self, A1 argument1, A2 argument2);
	}

	@FunctionalInterface
	public interface ProcedureTriple<T, A1, A2, A3> {
		void execute(T self, A1 argument1, A2 argument2, A3 argument3);
	}

	@FunctionalInterface
	public interface ProcedureVarargs<T, A> {
		@SuppressWarnings("unchecked")
		void execute(T self, A... arguments);
	}

	@FunctionalInterface
	public interface FunctionNone<T, R> {
		R execute(T self);
	}

	@FunctionalInterface
	public interface FunctionSingle<T, R, A1> {
		R execute(T self, A1 argument1);
	}

	@FunctionalInterface
	public interface FunctionDouble<T, R, A1, A2> {
		R execute(T self, A1 argument1, A2 argument2);
	}

	@FunctionalInterface
	public interface FunctionTriple<T, R, A1, A2, A3> {
		R execute(T self, A1 argument1, A2 argument2, A3 argument3);
	}

	@FunctionalInterface
	public interface FunctionVarargs<T, R, A> {
		@SuppressWarnings("unchecked")
		R execute(T self, A... arguments);
	}

	private final ProxyBuilder<T> proxyBuilder;

	public static <T> ReferenceExtractor<T> of(Class<T> sourceClass) {
		ProxyBuilder<T> proxyBuilder = ProxyBuilderFactory.withFeature(Feature.SUPERCLASS).ofType(sourceClass);
		return new ReferenceExtractor<>(proxyBuilder);
	}

	public Method extract(ProcedureNone<? super T> procedure) {
		return extractNone(procedure);
	}

	public <A1> Method extract(ProcedureSingle<? super T, A1> procedure) {
		return extractSingle(procedure);
	}

	public <A1, A2> Method extract(ProcedureDouble<? super T, A1, A2> procedure) {
		return extractDouble(procedure);
	}

	public <A1, A2, A3> Method extract(ProcedureTriple<? super T, A1, A2, A3> procedure) {
		return extractTriple(procedure);
	}

	public <A> Method extract(ProcedureVarargs<? super T, A> procedure) {
		return extractVarargs(procedure);
	}

	public <R> Method extractFunction(FunctionNone<? super T, R> procedure) {
		return extractNoneFunction(procedure);
	}

	public <R, A1> Method extractFunction(FunctionSingle<? super T, R, A1> procedure) {
		return extractSingleFunction(procedure);
	}

	public <R, A1, A2> Method extractFunction(FunctionDouble<? super T, R, A1, A2> procedure) {
		return extractDoubleFunction(procedure);
	}

	public <R, A1, A2, A3> Method extractFunction(FunctionTriple<? super T, R, A1, A2, A3> procedure) {
		return extractTripleFunction(procedure);
	}

	public <A, R> Method extractFunction(FunctionVarargs<? super T, R, A> procedure) {
		return extractVarargsFunction(procedure);
	}

	public Method extractNone(ProcedureNone<? super T> procedure) {
		MethodReference<? super T> reference =
				(self, arguments) -> procedure.execute(self);
		return extractGeneric(reference);
	}

	public <R> Method extractNoneFunction(FunctionNone<? super T, R> procedure) {
		MethodReference<? super T> reference =
				(self, arguments) -> procedure.execute(self);
		return extractGeneric(reference);
	}

	public <A1> Method extractSingle(ProcedureSingle<? super T, A1> procedure) {
		MethodReference<T> reference =
				(self, arguments) -> procedure.execute(self, null);
		return extractGeneric(reference);
	}

	public <A1, R> Method extractSingleFunction(FunctionSingle<? super T, R, A1> procedure) {
		MethodReference<T> reference =
				(self, arguments) -> procedure.execute(self, null);
		return extractGeneric(reference);
	}

	public <A1, A2> Method extractDouble(ProcedureDouble<? super T, A1, A2> procedure) {
		MethodReference<T> reference =
				(self, arguments) -> procedure.execute(self, null, null);
		return extractGeneric(reference);
	}

	public <R, A1, A2> Method extractDoubleFunction(FunctionDouble<? super T, R, A1, A2> procedure) {
		MethodReference<T> reference =
				(self, arguments) -> procedure.execute(self, null, null);
		return extractGeneric(reference);
	}

	public <A1, A2, A3> Method extractTriple(
			ProcedureTriple<? super T, A1, A2, A3> procedure) {
		MethodReference<T> reference =
				(self, arguments) -> procedure.execute(self, null, null, null);
		return extractGeneric(reference);
	}

	public <R, A1, A2, A3> Method extractTripleFunction(
			FunctionTriple<? super T, R, A1, A2, A3> procedure) {
		MethodReference<T> reference =
				(self, arguments) -> procedure.execute(self, null, null, null);
		return extractGeneric(reference);
	}

	public <A> Method extractVarargs(ProcedureVarargs<? super T, A> procedure) {
		@SuppressWarnings("unchecked")
		MethodReference<T> reference =
				(self, arguments) -> procedure.execute(self);
		return extractGeneric(reference);
	}

	public <R, A> Method extractVarargsFunction(FunctionVarargs<? super T, R, A> procedure) {
		@SuppressWarnings("unchecked")
		MethodReference<T> reference =
				(self, arguments) -> procedure.execute(self);
		return extractGeneric(reference);
	}

	private Method extractGeneric(MethodReference<? super T> method) {
		ProcedureTestingHandler<T> handler = ProcedureTestingHandler.create();
		T proxy = this.proxyBuilder.instantiate(handler);
		method.execute(proxy);
		return handler.extract();
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
