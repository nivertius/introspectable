package com.googlecode.perfectable.introspection.proxy;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import javax.annotation.Nullable;

import com.googlecode.perfectable.introspection.Methods;

public final class Invocation<T> {
	
	private static final Object[] EMPTY_ARGUMENTS = new Object[0];
	
	private final Method method;
	private final Object[] arguments;
	
	public static Invocation<?> of(Method method, @Nullable Object... arguments) {
		return new Invocation<>(method, arguments);
	}
	
	private Invocation(Method method, @Nullable Object... arguments) {
		this.method = method;
		this.arguments = arguments == null ? EMPTY_ARGUMENTS : arguments.clone();
	}
	
	public Object invokeAsStatic() throws Throwable {
		try {
			Object result = this.method.invoke(null, this.arguments);
			return result;
		}
		catch(InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
	public Object invokeOn(T receiver) throws Throwable {
		checkNotNull(receiver);
		try {
			Object result = this.method.invoke(receiver, this.arguments);
			return result;
		}
		catch(InvocationTargetException e) {
			throw e.getCause();
		}
	}
	
	public <X> Invocation<X> asSimilarTo(X similarInstance) throws NoSuchMethodException {
		@SuppressWarnings("unchecked")
		Class<X> similarClass = (Class<X>) similarInstance.getClass();
		return asSimilar(similarClass);
	}
	
	public <X> Invocation<X> asSimilar(Class<X> similarClass) throws NoSuchMethodException {
		Optional<Method> similarOption = Methods.similar(similarClass, this.method);
		Method similar = similarOption.orElseThrow(NoSuchMethodException::new);
		@SuppressWarnings("unchecked")
		Invocation<X> checked = (Invocation<X>) Invocation.of(similar, this.arguments);
		return checked;
	}
	
	public interface Decomposer {
		void method(Method method);
		
		void arguments(Object... arguments);
	}
	
	public void decompose(Decomposer decomposer) {
		decomposer.method(this.method);
		decomposer.arguments(this.arguments.clone());
	}
	
}
