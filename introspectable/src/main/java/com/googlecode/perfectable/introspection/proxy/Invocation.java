package com.googlecode.perfectable.introspection.proxy;


@FunctionalInterface
public interface Invocation {
	Object invoke() throws Throwable;
}
