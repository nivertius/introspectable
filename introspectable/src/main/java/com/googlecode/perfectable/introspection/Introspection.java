package com.googlecode.perfectable.introspection;

public final class Introspection<X> {
	public static <X> Introspection<X> of(Class<X> type) {
		return new Introspection<>(type);
	}

	public FieldQuery fields() {
		return FieldQuery.of(this.type);
	}
	
	public MethodQuery methods() {
		return MethodQuery.of(this.type);
	}

	public InterfaceQuery<X> interfaces() {
		return InterfaceQuery.of(this.type);
	}
	
	private final Class<X> type;
	
	private Introspection(Class<X> type) {
		this.type = type;
	}
}
