package org.perfectable.introspection;

import org.perfectable.introspection.query.AnnotationQuery;
import org.perfectable.introspection.query.ConstructorQuery;
import org.perfectable.introspection.query.FieldQuery;
import org.perfectable.introspection.query.InheritanceQuery;
import org.perfectable.introspection.query.MethodQuery;
import org.perfectable.introspection.query.RelatedTypeQuery;
import org.perfectable.introspection.type.ClassView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
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

	public ConstructorQuery<X> constructors() {
		return ConstructorQuery.of(this.type);
	}

	public MethodQuery methods() {
		return MethodQuery.of(this.type);
	}

	public InheritanceQuery<X> inheritance() {
		return InheritanceQuery.of(this.type);
	}

	public InheritanceQuery<X> interfaces() {
		return inheritance().onlyInterfaces();
	}

	public InheritanceQuery<X> superclasses() {
		return inheritance().onlyClasses();
	}

	public ClassView<X> view() {
		return ClassView.of(this.type);
	}

	public AnnotationQuery<Annotation> annotations() {
		return AnnotationQuery.of(this.type);
	}

	public RelatedTypeQuery related() {
		return RelatedTypeQuery.of(this.type);
	}

	public boolean isInstantiable() {
		// SUPPRESS NEXT BooleanExpressionComplexity
		return !type.isInterface()
				&& !type.isArray()
				&& (type.getModifiers() & Modifier.ABSTRACT) == 0
				&& !type.isPrimitive();
	}

	@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "ThrowSpecificExceptions"})
	public X instantiate() {
		checkState(isInstantiable(), "%s is not isInstantiable", type);
		try {
			return defaultConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <E extends X> Class<E> asGeneric() {
		return (Class<E>) type;
	}

	private ClassIntrospection(Class<X> type) {
		this.type = type;
	}

	@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "ThrowSpecificExceptions"})
	public Constructor<X> defaultConstructor() {
		try {
			Constructor<X> constructor = type.getDeclaredConstructor();
			PrivilegedActions.markAccessible(constructor);
			return constructor;
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		}
	}
}
