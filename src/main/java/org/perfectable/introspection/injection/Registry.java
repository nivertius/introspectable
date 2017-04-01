package org.perfectable.introspection.injection;

public interface Registry {

	<T> T fetch(Class<T> targetClass);
}
