package org.perfectable.introspection;

import org.perfectable.introspection.query.AnnotationQuery;
import org.perfectable.introspection.query.ConstructorQuery;
import org.perfectable.introspection.query.FieldQuery;
import org.perfectable.introspection.query.InheritanceQuery;
import org.perfectable.introspection.query.MethodQuery;
import org.perfectable.introspection.type.ClassView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static com.google.common.base.Preconditions.checkState;

/**
 * Entry point for class introspections.
 *
 * <p>Use {@link Introspections#introspect(Class)} to get instance of this class.
 *
 * @param <X> introspected class
 */
public final class ClassIntrospection<X> {
	static <X> ClassIntrospection<X> of(Class<X> type) {
		return new ClassIntrospection<>(type);
	}

	private final Class<X> type;

	/**
	 * Query for fields of introspected class.
	 *
	 * @return field query on introspected class.
	 */
	public FieldQuery fields() {
		return FieldQuery.of(this.type);
	}

	/**
	 * Query for constructors of introspected class.
	 *
	 * @return constructors query on introspected class.
	 */
	public ConstructorQuery<X> constructors() {
		return ConstructorQuery.of(this.type);
	}

	/**
	 * Query for methods of introspected class.
	 *
	 * @return methods query on introspected class.
	 */
	public MethodQuery methods() {
		return MethodQuery.of(this.type);
	}

	/**
	 * Query for implemented/extended interfaces/classes of introspected class.
	 *
	 * @return inheritance query on introspected class.
	 */
	public InheritanceQuery<X> inheritance() {
		return InheritanceQuery.of(this.type);
	}

	/**
	 * Query for implemented interfaces of introspected class.
	 *
	 * <p>This will list transitively implemented interfaces.
	 *
	 * @return query for interfaces of introspected class.
	 */
	public InheritanceQuery<X> interfaces() {
		return inheritance().onlyInterfaces();
	}

	/**
	 * Query for extended superclasses of introspected class.
	 *
	 * @return query for superclasses of introspected class.
	 */
	public InheritanceQuery<X> superclasses() {
		return inheritance().onlyClasses();
	}

	/**
	 * Query for runtime-visible annotations on introspected class.
	 *
	 * @return query for annotations of introspected class.
	 */
	public AnnotationQuery<Annotation> annotations() {
		return AnnotationQuery.of(this.type);
	}

	/**
	 * Wrap introspected class in {@link ClassView}.
	 *
	 * @return ClassView of the introspected class
	 */
	public ClassView<X> view() {
		return ClassView.of(this.type);
	}

	/**
	 * Introspect a classloader of this type.
	 *
	 * <p>This method deals with {@link Class#getClassLoader} returning potentially null, which indicates that
	 * this class was loaded by bootstrap classloader.
	 *
	 * @return introspection of this type classloader
	 */
	public ClassLoaderIntrospection classLoader() {
		return ClassLoaderIntrospection.of(type.getClassLoader());
	}

	/**
	 * Tests if the class is instantiable.
	 *
	 * <p>This checks if there is way to instantiate this class using some of its constructor.
	 *
	 * @return if the class can be instantiated using constructor
	 */
	@SuppressWarnings("BooleanExpressionComplexity")
	public boolean isInstantiable() {
		return !type.isInterface()
				&& !type.isArray()
				&& (type.getModifiers() & Modifier.ABSTRACT) == 0
				&& !type.isPrimitive();
	}

	/**
	 * Tries to create an instance of this class using parameterless constructor.
	 *
	 * <p>If the construction fails, unchecked exception is thrown.
	 *
	 * <p>This method should be only used when the class is assumed to have parameterless constructor. If this
	 * is not the case, and the constructor to use is unknown, use the {@link #constructors} to search for suitable
	 * one.
	 *
	 * @return new instance of a class
	 */
	@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "ThrowSpecificExceptions"})
	public X instantiate() {
		checkState(isInstantiable(), "%s is not isInstantiable", type);
		try {
			return parameterlessConstructor().newInstance();
		}
		catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException(e); // SUPPRESS JavadocMethod
		}
	}

	/**
	 * Gets parameterless constructor for introspected class.
	 *
	 * <p>This method also marks the constructor as {@link Constructor#setAccessible(boolean)} for immediate use.
	 *
	 * <p>This method should be only used when the class is assumed to have parameterless constructor. If this
	 * is not the case, and the constructor to use is unknown, use the {@link #constructors} to search for suitable
	 * one.
	 *
	 * @return parameterless constructor
	 */
	@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "ThrowSpecificExceptions"})
	public Constructor<X> parameterlessConstructor() {
		try {
			Constructor<X> constructor = type.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor;
		}
		catch (NoSuchMethodException e) {
			throw new RuntimeException(e); // SUPPRESS JavadocMethod
		}
	}

	/**
	 * Allows cast-less conversion from raw class to either generic form, or parametrized class.
	 *
	 * <p>This function exists, because creating ClassIntrospection from class literal, either by
	 * {@link ClassIntrospection#of}, or {@link Introspections#introspect(Class)} will produce
	 * introspection with raw type as argument. When provided literal is a generic class, produced type should
	 * parameterized with unbounded wildcards, but isn't. This method allows adjusting the type easily.
	 *
	 * <p>Example:
	 * <pre>
	 * ClassIntrospection&lt;List&gt; rawIntrospection = ClassIntrospection.of(List.class);
	 * ClassIntrospection&lt;List&lt;?&gt;&gt; genericIntrospection = rawView.adjustWildcards();
	 * </pre>
	 *
	 * <p>This method is equivalent to just casting to parameterized class with wildcards,
	 * but without unchecked warning.
	 *
	 * <p>WARNING: This method can be used to cast to inheriting types, i.e.
	 * {@code ClassIntrospection<ArrayList<Number>>} in previous example. If you are concerned that this
	 * might be the case, avoid this method, its only for convenience.
	 *
	 * @param <E> parameterized type to cast to
	 * @return casted class introspection
	 */
	@SuppressWarnings("unchecked")
	public <E extends X> Class<E> asGeneric() {
		return (Class<E>) type;
	}

	private ClassIntrospection(Class<X> type) {
		this.type = type;
	}

}
