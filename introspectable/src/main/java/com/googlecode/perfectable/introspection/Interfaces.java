package com.googlecode.perfectable.introspection;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Interfaces {

	public static Iterable<Class<?>> of(Class<?> sourceClass) {
		Set<Class<?>> result = new HashSet<>();
		for(Class<?> currentClass : InheritanceChain.startingAt(sourceClass)) {
			Collections.addAll(result, currentClass.getInterfaces());
		}
		return result;
	}

}
