package org.perfectable.introspection;

import org.perfectable.introspection.query.FieldQuery;
import org.perfectable.introspection.query.GenericsQuery;
import org.perfectable.introspection.query.InterfaceQuery;
import org.perfectable.introspection.query.MethodQuery;

public final class ClassIntrospection<X> {
	static <X> ClassIntrospection<X> of(Class<X> type) {
		return new ClassIntrospection<>(type);
	}

	private final Class<X> type;

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

	private ClassIntrospection(Class<X> type) {
		this.type = type;
	}

}
