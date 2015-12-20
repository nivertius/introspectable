package com.googlecode.perfectable.introspection;

import com.googlecode.perfectable.introspection.query.FieldQuery;
import com.googlecode.perfectable.introspection.query.GenericsQuery;
import com.googlecode.perfectable.introspection.query.InterfaceQuery;
import com.googlecode.perfectable.introspection.query.MethodQuery;

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
	
	public GenericsQuery<X> generics() {
		return GenericsQuery.of(this.type);
	}

	public RelatedClassesIterable related() {
		return RelatedClassesIterable.of(this.type);
	}

	private final Class<X> type;
	
	private Introspection(Class<X> type) {
		this.type = type;
	}
}
