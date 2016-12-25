package org.perfectable.introspection;

import org.perfectable.introspection.query.AnnotationQuery;
import org.perfectable.introspection.query.FieldQuery;
import org.perfectable.introspection.query.GenericsQuery;
import org.perfectable.introspection.query.InterfaceQuery;
import org.perfectable.introspection.query.MethodQuery;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static com.google.common.base.Preconditions.checkState;

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

	public AnnotationQuery<Annotation> annotations() {
		return AnnotationQuery.of(this.type);
	}

	public ReferenceExtractor<X> references() {
		return ReferenceExtractor.of(type);
	}

	public RelatedClassesIterable related() {
		return RelatedClassesIterable.of(this.type);
	}

	public boolean isInstantiable() {
		// SUPPRESS NEXT BooleanExpressionComplexity
		return !type.isInterface()
				&& !type.isArray()
				&& (type.getModifiers() & Modifier.ABSTRACT) == 0
				&& !type.isPrimitive();
	}

	public X instantiate() {
		checkState(isInstantiable(), "%s is not isInstantiable", type);
		try {
			return type.getConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
			throw new RuntimeException(e); // SUPPRESS no better exception here
		}
	}

	@SuppressWarnings("unchecked")
	public <E extends X> Class<E> asGeneric() {
		return (Class<E>) type;
	}

	private ClassIntrospection(Class<X> type) {
		this.type = type;
	}

}
