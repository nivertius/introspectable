package com.googlecode.perfectable.introspection;

public final class Casts {

	@SuppressWarnings("unchecked")
	public static final <E> Class<E> generic(Class<? super E> casted) {
		return (Class<E>) casted;
	}
	
	private Casts() {
	}

}
