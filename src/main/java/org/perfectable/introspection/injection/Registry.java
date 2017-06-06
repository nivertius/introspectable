package org.perfectable.introspection.injection;

public interface Registry {
	<T> T fetch(Query<T> query);
}
