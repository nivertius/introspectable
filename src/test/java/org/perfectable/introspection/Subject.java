package org.perfectable.introspection;

import java.lang.reflect.Method;

import javax.annotation.Nullable;

import static org.perfectable.introspection.Methods.get;

public class Subject {
	public static final Method NO_RESULT_NO_ARGUMENT =
			get(Subject.class, "noResultNoArgument");
	public static final Method NO_RESULT_SINGLE_ARGUMENT =
			get(Subject.class, "noResultSingleArgument", Object.class);
	public static final Method NO_RESULT_DOUBLE_ARGUMENT =
			get(Subject.class, "noResultDoubleArgument", Object.class, Object.class);
	public static final Method NO_RESULT_TRIPLE_ARGUMENT =
			get(Subject.class, "noResultTripleArgument",
					Object.class, Object.class, Object.class);
	public static final Method NO_RESULT_VARARGS_ARGUMENT =
			get(Subject.class, "noResultVarargsArgument", Object[].class);

	public static final Method WITH_RESULT_NO_ARGUMENT =
			get(Subject.class, "withResultNoArgument");
	public static final Method WITH_RESULT_SINGLE_ARGUMENT =
			get(Subject.class, "withResultSingleArgument", Object.class);
	public static final Method WITH_RESULT_DOUBLE_ARGUMENT =
			get(Subject.class, "withResultDoubleArgument", Object.class, Object.class);
	public static final Method WITH_RESULT_TRIPLE_ARGUMENT =
			get(Subject.class, "withResultTripleArgument",
					Object.class, Object.class, Object.class);
	public static final Method WITH_RESULT_VARARGS_ARGUMENT =
			get(Subject.class, "withResultVarargsArgument", Object[].class);
	public static final Method METHOD_PROTECTED =
			get(Subject.class, "methodProtected");
	public static final Method ANNOTATED_WITH_NULLABLE =
			get(Subject.class, "annotatedWithNullable");

	public void noResultNoArgument() {
		throw new AssertionError("Test method should not be called");
	}

	public void noResultSingleArgument(Object argument1) {
		throw new AssertionError("Test method should not be called");
	}

	public void noResultDoubleArgument(Object argument1, Object argument2) {
		throw new AssertionError("Test method should not be called");
	}

	public void noResultTripleArgument(Object argument1, Object argument2, Object argument3) {
		throw new AssertionError("Test method should not be called");
	}

	public void noResultVarargsArgument(Object... arguments) {
		throw new AssertionError("Test method should not be called");
	}

	public Object withResultNoArgument() {
		throw new AssertionError("Test method should not be called");
	}

	public Object withResultSingleArgument(Object argument1) {
		throw new AssertionError("Test method should not be called");
	}

	public Object withResultDoubleArgument(Object argument1, Object argument2) {
		throw new AssertionError("Test method should not be called");
	}

	public Object withResultTripleArgument(Object argument1, Object argument2, Object argument3) {
		throw new AssertionError("Test method should not be called");
	}

	public Object withResultVarargsArgument(Object... arguments) {
		throw new AssertionError("Test method should not be called");
	}

	protected void methodProtected() {
		throw new AssertionError("Test method should not be called");
	}


	@Nullable
	public Object annotatedWithNullable() {
		throw new AssertionError("Test method should not be called");
	}

}
