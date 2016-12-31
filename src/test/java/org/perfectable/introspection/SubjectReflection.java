package org.perfectable.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.perfectable.introspection.SimpleReflections.getField;
import static org.perfectable.introspection.SimpleReflections.getMethod;

public class SubjectReflection {
	public static final Field STRING_FIELD =
			getField(Subject.class, "stringField");
	public static final Field OBJECT_FIELD =
			getField(Subject.class, "objectField");
	public static final Field STATIC_FIELD =
			getField(Subject.class, "STATIC_FIELD");
	public static final Field PROTECTED_NUMBER_FIELD =
			getField(Subject.class, "protectedNumberField");

	public static final Method NO_RESULT_NO_ARGUMENT =
			getMethod(Subject.class, "noResultNoArgument");
	public static final Method NO_RESULT_SINGLE_ARGUMENT =
			getMethod(Subject.class, "noResultSingleArgument", Object.class);
	public static final Method NO_RESULT_DOUBLE_ARGUMENT =
			getMethod(Subject.class, "noResultDoubleArgument", Object.class, Object.class);
	public static final Method NO_RESULT_TRIPLE_ARGUMENT =
			getMethod(Subject.class, "noResultTripleArgument",
					Object.class, Object.class, Object.class);
	public static final Method NO_RESULT_VARARGS_ARGUMENT =
			getMethod(Subject.class, "noResultVarargsArgument", Object[].class);
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
}
