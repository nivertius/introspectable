package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import com.googlecode.perfectable.introspection.Methods;

public final class Invocation<T> {
	
	private final Method method;
	private final Object[] arguments;
	
	public Invocation(Method method, Object[] arguments) {
		this.method = method;
		this.arguments = arguments;
	}
	
	public static Invocation<?> of(Method method, Object[] arguments) {
		return new Invocation<>(method, arguments);
	}

	public Object invokeOn(T receiver) throws Throwable {
		try {
			Object result = this.method.invoke(receiver, this.arguments);
			return result;
		}
		catch(InvocationTargetException e) {
			throw e.getCause();
		}
		catch(IllegalAccessException | IllegalArgumentException e) {
			throw new RuntimeException(e);
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

}
