package org.perfectable.introspection.query;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.perfectable.introspection.SimpleReflections.getConstructor;
import static org.perfectable.introspection.SimpleReflections.getField;
import static org.perfectable.introspection.SimpleReflections.getMethod;

public final class SubjectReflection {
	static final String MESSAGE_METHOD_CALLED = "Test method should not be called";
	static final String MESSAGE_CONSTRUCTOR_CALLED = "Test constructor should not be called";

	public static final Field STRING_FIELD =
		getField(Subject.class, "stringField");
	public static final Field OBJECT_FIELD =
		getField(Subject.class, "objectField");
	public static final Field STATIC_FIELD =
		getField(Subject.class, "staticField");
	public static final Field PROTECTED_NUMBER_FIELD =
		getField(Subject.class, "protectedNumberField");
	public static final Field NESTED_INTERFACE_FIELD =
		getField(Subject.NestedInterface.class, "STATIC_FIELD");

	public static final Constructor<Subject> CONSTRUCTOR_NO_ARGS =
		getConstructor(Subject.class);
	public static final Constructor<Subject> CONSTRUCTOR_STRING =
		getConstructor(Subject.class, String.class);
	public static final Constructor<Subject> CONSTRUCTOR_ANNOTATED =
		getConstructor(Subject.class, Number.class);
	public static final Constructor<Subject> CONSTRUCTOR_PROTECTED =
		getConstructor(Subject.class, Object.class, Object.class);

	public static final Method NO_RESULT_NO_ARGUMENT =
		getMethod(Subject.class, "noResultNoArgument");
	public static final Method NO_RESULT_SINGLE_ARGUMENT =
		getMethod(Subject.class, "noResultSingleArgument", Object.class);
	public static final Method NO_RESULT_PRIMITIVE_ARGUMENT =
		getMethod(Subject.class, "noResultPrimitiveArgument", int.class);
	public static final Method NO_RESULT_STRING_ARGUMENT =
		getMethod(Subject.class, "noResultStringArgument", String.class);
	public static final Method NO_RESULT_DOUBLE_ARGUMENT =
		getMethod(Subject.class, "noResultDoubleArgument", Object.class, Object.class);
	public static final Method NO_RESULT_STRING_NUMBER_ARGUMENT =
		getMethod(Subject.class, "noResultStringNumberArgument", String.class, Number.class);
	public static final Method NO_RESULT_TRIPLE_ARGUMENT =
		getMethod(Subject.class, "noResultTripleArgument",
			Object.class, Object.class, Object.class);
	public static final Method NO_RESULT_VARARGS_ARGUMENT =
		getMethod(Subject.class, "noResultVarargsArgument", Object[].class);
	public static final Method NO_RESULT_VARARGS_DOUBLE_ARGUMENT =
		getMethod(Subject.class, "noResultVarargsDoubleArgument", Object.class, Object.class, Object[].class);
	public static final Method WITH_RESULT_NO_ARGUMENT =
		getMethod(Subject.class, "withResultNoArgument");
	public static final Method WITH_RESULT_SINGLE_ARGUMENT =
		getMethod(Subject.class, "withResultSingleArgument", Object.class);
	public static final Method WITH_RESULT_DOUBLE_ARGUMENT =
		getMethod(Subject.class, "withResultDoubleArgument", Object.class, Object.class);
	public static final Method WITH_RESULT_TRIPLE_ARGUMENT =
		getMethod(Subject.class, "withResultTripleArgument",
			Object.class, Object.class, Object.class);
	public static final Method WITH_RESULT_VARARGS_ARGUMENT =
		getMethod(Subject.class, "withResultVarargsArgument", Object[].class);
	public static final Method METHOD_PROTECTED =
		getMethod(Subject.class, "methodProtected");
	public static final Method ANNOTATED_WITH_NULLABLE =
		getMethod(Subject.class, "annotatedWithNullable");
	public static final Method TO_STRING =
		getMethod(Subject.class, "toString");

	private SubjectReflection() {
		// utility class
	}
}
