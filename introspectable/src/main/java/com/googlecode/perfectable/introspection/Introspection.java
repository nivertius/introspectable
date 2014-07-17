package com.googlecode.perfectable.introspection;

public final class Introspection<X> {

	private final Class<X> type;

	public static <X> Introspection<X> of(Class<X> type) {
		return new Introspection<>(type);
	}

	private Introspection(Class<X> type) {
		this.type = type;
	}

	public FieldQuery fields() {
		return FieldQuery.of(this.type);
	}
	
	public MethodQuery methods() {
		return MethodQuery.of(this.type);
	}

}
