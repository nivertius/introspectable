package com.googlecode.perfectable.introspection.proxy;

import java.lang.reflect.Method;

public interface Invocation {
	Object invoke() throws Throwable;
	
	interface Decomposer {
		void method(Method method);
		
		void receiver(Object receiver);
		
		<T> void argument(int index, Class<? super T> formal, T actual);
	}
}
