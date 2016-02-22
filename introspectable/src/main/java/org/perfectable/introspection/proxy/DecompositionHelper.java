package org.perfectable.introspection.proxy;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

final class DecompositionHelper {
	
	public interface ArgumentConsumer {
		<X> void consume(int index, Class<? super X> formal, X actual);
	}
	
	public static void decomposeArguments(Method method, Object[] arguments, ArgumentConsumer argumentConsumer) {
		Parameter[] parameters = method.getParameters();
		int i = 0;
		for(Object argument : arguments) {
			Class<?> formal;
			if(i < parameters.length) {
				formal = parameters[i].getType();
			}
			else {
				Parameter lastParameter = parameters[parameters.length - 1];
				formal = lastParameter.getType();
			}
			@SuppressWarnings("unchecked")
			Class<? super Object> casted = (Class<? super Object>) formal;
			argumentConsumer.consume(i, casted, argument);
			i++;
		}
	}
	
	private DecompositionHelper() {
		// utility class
	}
	
}
