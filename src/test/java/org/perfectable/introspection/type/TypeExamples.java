package org.perfectable.introspection.type; // SUPPRESS LENGTH

import java.io.Externalizable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.function.Supplier;

import static org.perfectable.introspection.SimpleReflections.getField;
import static org.perfectable.introspection.SimpleReflections.getMethod;

@SuppressWarnings({"unused", "nullness:initialization.field.uninitialized"})
final class TypeExamples {
	interface Root<X> {
		TypeVariable<?> CLASS_FIRST_VARIABLE = Root.class.getTypeParameters()[0];

		Method PARAMETER_WITH_GENERIC_TYPE_METHOD =
			getMethod(Root.class, "methodWithTypeParameter", Object.class);
		Method OWN_PARAMETER_METHOD =
			getMethod(Root.class, "methodWithOwnParameter", Object.class);
		Method MULTIPLE_BOUNDS_METHOD =
			getMethod(Root.class, "methodWithMultipleBoundsParameter", Number.class);
		TypeVariable<?> MULTIPLE_BOUNDS_METHOD_FIRST_VARIABLE = MULTIPLE_BOUNDS_METHOD.getTypeParameters()[0];

		Method EXTENDS_TYPE_PARAMETER_METHOD =
			getMethod(Root.class, "methodWithExtendsTypeParameter", Object.class);
		TypeVariable<?> EXTENDS_TYPE_PARAMETER_METHOD_FIRST_VARIABLE = // SUPPRESS LongVariable
			EXTENDS_TYPE_PARAMETER_METHOD.getTypeParameters()[0];

		void methodWithTypeParameter(X parameter);

		<Y> void methodWithOwnParameter(Y parameter);

		<Y extends Number & Externalizable & Type> void methodWithMultipleBoundsParameter(Y parameter);

		<Y extends X> void methodWithExtendsTypeParameter(Y parameter);
	}

	@SuppressWarnings("nullness:initialization.fields.uninitialized")
	abstract static class Unbounded<U extends Number> implements Root<U> {
		static final TypeVariable<?> CLASS_FIRST_VARIABLE = Unbounded.class.getTypeParameters()[0];

		static final Field GENERIC_WITH_CONSTANT_FIELD = getField(Unbounded.class, "genericWithConstantField");
		static final Field GENERIC_WITH_PARAMETER_FIELD = getField(Unbounded.class, "genericWithParameterField");

		static final Field UNBOUNDED_WILDCARD_FIELD = getField(Unbounded.class, "unboundedWildcardField");
		static final Type UNBOUNDED_WILDCARD_FIELD_TYPE_FIRST_ARGUMENT = // SUPPRESS LongVariable
			((ParameterizedType) UNBOUNDED_WILDCARD_FIELD.getGenericType()).getActualTypeArguments()[0];

		static final Field LOWER_BOUNDED_WILDCARD_FIELD = getField(Unbounded.class, "lowerBoundedWildcardField");
		static final Type LOWER_BOUNDED_WILDCARD_FIELD_FIRST_ARGUMENT = // SUPPRESS LongVariable
			((ParameterizedType) LOWER_BOUNDED_WILDCARD_FIELD.getGenericType()).getActualTypeArguments()[0];

		static final Field UPPER_BOUNDED_WILDCARD_FIELD = getField(Unbounded.class, "upperBoundedWildcardField");
		static final Type UPPER_BOUNDED_WILDCARD_FIELD_FIRST_ARGUMENT = // SUPPRESS LongVariable
			((ParameterizedType) UPPER_BOUNDED_WILDCARD_FIELD.getGenericType()).getActualTypeArguments()[0];

		static final Field BOUNDED_VARIABLE_WILDCARD_FIELD = getField(Unbounded.class, "boundedVariableWildcardField");
		static final Field BOUNDED_WILDCARD_RESOLVING_FIELD = getField(Unbounded.class, "boundedWildcardResolving");

		static final Type BOUNDED_WILDCARD_RESOLVING_FIELD_TYPE_FIRST_ARGUMENT = // SUPPRESS LongVariable
			((ParameterizedType) BOUNDED_WILDCARD_RESOLVING_FIELD.getGenericType()).getActualTypeArguments()[0];

		static final Field NONCANONICAL_WILDCARD_FIELD = getField(Unbounded.class, "nonCanonicalWildcardField");
		static final Field NONCANONICAL_BOUNDED_WILDCARD_FIELD =
			getField(Unbounded.class, "nonCanonicalBoundedWildcardField");

		static final Field GENERIC_ARRAY_FIELD = getField(Unbounded.class, "genericArrayField");

		static final Method VARIABLE_RESOLVING_METHOD =
			getMethod(Unbounded.class, "variableResolvingMethod", Unbounded.class);
		static final TypeVariable<?> VARIABLE_RESOLVING_METHOD_FIRST_VARIABLE = // SUPPRESS LongVariable
			VARIABLE_RESOLVING_METHOD.getTypeParameters()[0];

		Supplier<?> unboundedWildcardField; // SUPPRESS VisibilityModifier test only

		Supplier<? super Number> lowerBoundedWildcardField; // SUPPRESS VisibilityModifier test only

		Supplier<? extends Number> upperBoundedWildcardField; // SUPPRESS VisibilityModifier test only

		Supplier<? extends U> boundedVariableWildcardField; // SUPPRESS VisibilityModifier test only

		Supplier<? extends Unbounded<Integer>> boundedWildcardResolving;  // SUPPRESS VisibilityModifier test only

		Supplier<String> genericWithConstantField; // SUPPRESS VisibilityModifier test only

		Supplier<U> genericWithParameterField; // SUPPRESS VisibilityModifier test only

		Unbounded<?> nonCanonicalWildcardField; // SUPPRESS VisibilityModifier test only

		Unbounded<? extends Externalizable> nonCanonicalBoundedWildcardField; // SUPPRESS VisibilityModifier test only

		U[] genericArrayField; // SUPPRESS VisibilityModifier test only

		abstract <F extends Unbounded<Double>> void variableResolvingMethod(F parameter);

		abstract class FirstParameterSupplier implements Supplier<U> {

		}
	}

	@SuppressWarnings("serial")
	abstract static class ExternalizableNumber extends Number implements Externalizable {
		// test interface
	}

	@SuppressWarnings("serial")
	abstract static class ExternalizableTypeNumber extends ExternalizableNumber
		implements Type, Comparable<ExternalizableTypeNumber> {
		// test interface
	}

	abstract static class ExternalizableTypeNumberBounded extends Unbounded<ExternalizableTypeNumber> {
		// test interface
	}

	abstract static class ExternalizableUnbounded<E extends Number & Externalizable>
		extends Unbounded<E> {
		// test interface
	}

	abstract static class Bounded extends Unbounded<Long> {
		// test interface
	}

	abstract static class LongSupplier implements Supplier<Long> {
		// test interface
	}

	abstract static class StringSupplier implements Supplier<String> {
		// test interface
	}

	private TypeExamples() {
		// example container
	}
}
