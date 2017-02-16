package org.perfectable.introspection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Nullable;

import static org.perfectable.introspection.SubjectReflection.MESSAGE_METHOD_CALLED;

@Subject.Special
@Subject.OtherAnnotation
public class Subject {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Special {

	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface OtherAnnotation {

	}

	private String stringField;

	@Nullable
	private Object objectField;

	protected final Number protectedNumberField = 191;

	@Nullable
	public static Subject STATIC_FIELD;

	public void noResultNoArgument() {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public void noResultSingleArgument(Object argument1) {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public void noResultDoubleArgument(Object argument1, Object argument2) {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public void noResultTripleArgument(Object argument1, Object argument2, Object argument3) {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public void noResultVarargsArgument(Object... arguments) {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public Object withResultNoArgument() {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public Object withResultSingleArgument(Object argument1) {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public Object withResultDoubleArgument(Object argument1, Object argument2) {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public Object withResultTripleArgument(Object argument1, Object argument2, Object argument3) {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public Object withResultVarargsArgument(Object... arguments) {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	protected void methodProtected() {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}


	@Nullable
	public Object annotatedWithNullable() {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

}
