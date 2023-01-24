package org.perfectable.introspection.query;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.perfectable.introspection.query.SubjectReflection.MESSAGE_CONSTRUCTOR_CALLED;
import static org.perfectable.introspection.query.SubjectReflection.MESSAGE_METHOD_CALLED;

@SuppressWarnings({"unused", "InterfaceWithOnlyStatics", "nullness:initialization.static.field.uninitialized"})
@Subject.Special
@Subject.OtherAnnotation
@Subject.Repetition(1)
@Subject.Repetition(2)
public class Subject {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface Special {

	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface OtherAnnotation {

	}

	@Repeatable(RepetitionContainer.class)
	@Retention(RetentionPolicy.RUNTIME)
	public @interface Repetition {
		int value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	public @interface RepetitionContainer {
		Repetition[] value();
	}

	private String stringField;

	@Deprecated
	private Object objectField;

	protected final Number protectedNumberField = 191; // SUPPRESS used for testing

	// SUPPRESS NEXT 2 StaticVariableName used for testing
	@Deprecated
	public static Subject staticField; // SUPPRESS VisibilityModifier used for testing

	public Subject() {
		throw new AssertionError(MESSAGE_CONSTRUCTOR_CALLED);
	}

	public Subject(String argument1) {
		throw new AssertionError(MESSAGE_CONSTRUCTOR_CALLED);
	}

	@Special
	public Subject(Number argument1) {
		throw new AssertionError(MESSAGE_CONSTRUCTOR_CALLED);
	}

	protected Subject(Object argument1, Object argument2) {
		throw new AssertionError(MESSAGE_CONSTRUCTOR_CALLED);
	}

	public void noResultNoArgument() {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public void noResultPrimitiveArgument(int argument1) {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public void noResultSingleArgument(Object argument1) {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public void noResultStringArgument(String argument1) {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	public void noResultStringNumberArgument(String argument1, Number argument2) {
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

	public void noResultVarargsDoubleArgument(Object argument1, Object argument2, Object... arguments) {
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

	@SuppressWarnings("static-method")
	void methodPackage() {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	@SuppressWarnings("static-method")
	private void methodPrivate() {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	@OtherAnnotation
	public Object annotatedMethod() {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}

	@Override
	public String toString() {
		throw new AssertionError(MESSAGE_METHOD_CALLED);
	}
	
	@SuppressWarnings("unused") // SUPPRESS InterfaceIsType
	public interface NestedInterface {
		String STATIC_FIELD = "staticFieldValue"; // SUPPRESS ConstantInIntarface
	}

	public static class Extension extends Subject {
		@Override
		public void noResultNoArgument() {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}

		@Override
		protected void methodProtected() {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}

		@Override
		void methodPackage() {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}

		@SuppressWarnings("static-method")
		private void methodPrivate() {
			throw new AssertionError(MESSAGE_METHOD_CALLED);
		}

	}
}
