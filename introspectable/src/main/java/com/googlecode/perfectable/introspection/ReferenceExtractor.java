package com.googlecode.perfectable.introspection;

import static com.google.common.base.Preconditions.checkState;

import javax.annotation.Nullable;

import com.google.common.base.Defaults;
import com.googlecode.perfectable.introspection.proxy.BoundInvocation;
import com.googlecode.perfectable.introspection.proxy.InvocationHandler;
import com.googlecode.perfectable.introspection.proxy.MethodBoundInvocation;
import com.googlecode.perfectable.introspection.proxy.MethodInvocable;
import com.googlecode.perfectable.introspection.proxy.ProxyBuilder;
import com.googlecode.perfectable.introspection.proxy.ProxyBuilderFactory;
import com.googlecode.perfectable.introspection.proxy.ProxyBuilderFactory.Feature;

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
	
	public MethodInvocable extractGeneric(GenericMethodReference<T> method) {
		ProcedureTestingHandler<T> handler = ProcedureTestingHandler.create();
		T proxy = this.proxyBuilder.instantiate(handler);
		method.execute(proxy);
		return handler.extract();
	}
	
	public MethodInvocable extractNone(NoArgumentMethodReference<T> procedure) {
		GenericMethodReference<T> reference =
				(self, arguments) -> procedure.execute(self);
		return extractGeneric(reference);
	}
	
	public <A1> MethodInvocable extractSingle(SingleArgumentMethodReference<? super T, A1> procedure) {
		GenericMethodReference<T> reference =
				(self, arguments) -> procedure.execute(self, null);
		return extractGeneric(reference);
	}
	
	public <A1, A2> MethodInvocable extractDouble(DoubleArgumentMethodReference<T, A1, A2> procedure) {
		GenericMethodReference<T> reference =
				(self, arguments) -> procedure.execute(self, null, null);
		return extractGeneric(reference);
	}
	
	private ReferenceExtractor(ProxyBuilder<T> proxyBuilder) {
		this.proxyBuilder = proxyBuilder;
	}
	
	private final static class ProcedureTestingHandler<T> implements InvocationHandler<T> {
		@Nullable
		private MethodInvocable executedInvocable;
		
		public static <T> ProcedureTestingHandler<T> create() {
			return new ProcedureTestingHandler<>();
		}
		
		public MethodInvocable extract() {
			checkState(this.executedInvocable != null);
			return this.executedInvocable;
		}
		
		@Override
		public Object handle(BoundInvocation<T> invocation) throws Throwable {
			checkState(this.executedInvocable == null);
			MethodBoundInvocation<T> methodInvocation = (MethodBoundInvocation<T>) invocation;
			this.executedInvocable = methodInvocation.stripArguments().stripReceiver();
			Class<?> expectedResultType = this.executedInvocable.expectedResultType();
			return Defaults.defaultValue(expectedResultType);
		}
		
	}
}
