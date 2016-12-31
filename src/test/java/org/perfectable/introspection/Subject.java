package org.perfectable.introspection;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.annotation.Nullable;

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
