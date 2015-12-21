package com.googlecode.perfectable.introspection;

public class Interfaces {
	
	@Deprecated
	public static <X> Iterable<Class<? super X>> of(Class<X> sourceClass) {
		return Introspection.of(sourceClass).interfaces();
	}
	
}
