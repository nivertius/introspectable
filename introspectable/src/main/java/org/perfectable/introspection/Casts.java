package org.perfectable.introspection;

public final class Casts {
	
	@SuppressWarnings("unchecked")
	public static <E> Class<E> generic(Class<? super E> casted) {
		return (Class<E>) casted;
	}
	
	private Casts() {
	}
	
}
