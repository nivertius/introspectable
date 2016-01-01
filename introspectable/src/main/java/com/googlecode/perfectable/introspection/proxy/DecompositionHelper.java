package com.googlecode.perfectable.introspection.proxy;

import static com.google.common.base.Preconditions.checkState;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import javax.annotation.Nullable;

import com.googlecode.perfectable.introspection.proxy.Invocation.Decomposer;

final class DecompositionHelper {
	
	private final Decomposer decomposer;
	@Nullable
	private Parameter[] parameters;
	
	public DecompositionHelper(Decomposer decomposer) {
		this.decomposer = decomposer;
	}
	
	public static DecompositionHelper start(Decomposer decomposer) {
		return new DecompositionHelper(decomposer);
	}
	
	public DecompositionHelper method(Method method) {
		this.parameters = method.getParameters();
		this.decomposer.method(method);
		return this;
	}
	
	public DecompositionHelper receiver(Object receiver) {
		this.decomposer.receiver(receiver);
		return this;
	}
	
	public DecompositionHelper arguments(Object... arguments) {
		checkState(this.parameters != null);
		int i = 0;
		for(Object argument : arguments) {
			Class<?> formal;
			if(i < this.parameters.length) {
				formal = this.parameters[i].getType();
			}
			else {
				Parameter lastParameter = this.parameters[this.parameters.length - 1];
				formal = lastParameter.getType();
			}
			@SuppressWarnings("unchecked")
			Class<? super Object> casted = (Class<? super Object>) formal;
			this.decomposer.argument(i, casted, argument);
			i++;
		}
		return this;
	}
	
}
