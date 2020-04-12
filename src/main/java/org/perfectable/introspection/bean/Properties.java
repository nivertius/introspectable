package org.perfectable.introspection.bean; // SUPPRESS LENGTH

import org.perfectable.introspection.PrivilegedActions;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

import com.google.common.base.Throwables;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.perfectable.introspection.Introspections.introspect;

final class Properties {
	private static final String BOOLEAN_GETTER_PREFIX = "is";
	private static final String STANDARD_GETTER_PREFIX = "get";
	private static final String SETTER_PREFIX = "set";

	static <T> Property<T, Object> fromField(Field field) {
		PrivilegedActions.markAccessible(field);
		return new FieldProperty<>(field);
	}

	static <CX> Property<CX, Object> create(Class<CX> beanClass, String name) {
		requireNonNull(beanClass);
		Optional<Field> field = introspect(beanClass).fields().named(name).option();
		if (field.isPresent()) {
			return fromField(field.get());
		}
		Optional<Method> getterOption = findGetter(beanClass, name);
		Optional<Method> setterOption = findSetter(beanClass, name);
		if (setterOption.isPresent() && getterOption.isPresent()) {
			Method getter = getterOption.get();
			Method setter = setterOption.get();
			ReadWriteMethodProperty.checkCompatibility(getter, setter);
			return new ReadWriteMethodProperty<>(getter, setter);
		}
		if (getterOption.isPresent()) {
			Method getter = getterOption.get();
			PrivilegedActions.markAccessible(getter);
			return new ReadOnlyMethodProperty<>(getter);
		}
		if (setterOption.isPresent()) {
			Method setter = setterOption.get();
			PrivilegedActions.markAccessible(setter);
			return new WriteOnlyMethodProperty<>(setter);
		}
		throw new IllegalArgumentException("No property " + name + " for " + beanClass);
	}

	private static Optional<Method> findGetter(Class<?> beanClass, String name) {
		Pattern getterNamePattern = Pattern.compile("(?:is|get)" + Pattern.quote(capitalize(name)));
		return introspect(beanClass).methods()
			.nameMatching(getterNamePattern)
			.parameters()
			.option();
	}

	private static Optional<Method> findSetter(Class<?> beanClass, String name) {
		String setterName = setterName(name);
		return introspect(beanClass).methods()
			.named(setterName)
			.parameterCount(1)
			.returningVoid()
			.option();
	}

	private static String propertyNameFromGetter(Method getter) {
		String unformatted = getter.getName();
		int prefixLength = getterPrefix(getter.getReturnType()).length();
		return String.valueOf(unformatted.charAt(prefixLength)).toLowerCase(Locale.ROOT)
			+ unformatted.substring(prefixLength + 1);
	}

	private static String propertyNameFromSetter(Method setter) {
		String unformatted = setter.getName();
		int prefixLength = SETTER_PREFIX.length();
		return String.valueOf(unformatted.charAt(prefixLength)).toLowerCase(Locale.ROOT)
			+ unformatted.substring(prefixLength + 1);
	}

	private static String setterName(String name) {
		return SETTER_PREFIX + capitalize(name);
	}

	private static String getterPrefix(Class<?> returnType) {
		boolean returnsBoolean = Boolean.class.equals(returnType)
			|| boolean.class.equals(returnType);
		return returnsBoolean ? BOOLEAN_GETTER_PREFIX : STANDARD_GETTER_PREFIX;
	}

	private static String capitalize(String name) {
		return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
	}

	static final class FieldProperty<CT, PT> extends Property<CT, PT> {
		private final Field field;

		FieldProperty(Field field) {
			this.field = field;
		}

		@Override
		@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "ThrowSpecificExceptions"})
		void set(CT bean, @Nullable PT value) {
			try {
				this.field.set(bean, value);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}


		// checked at construction
		@Override
		@Nullable
		@SuppressWarnings({"unchecked", "PMD.AvoidThrowingRawExceptionTypes", "ThrowSpecificExceptions"})
		PT get(CT bean) {
			try {
				return (PT) this.field.get(bean);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public String name() {
			return this.field.getName();
		}

		// checked at construction
		@Override
		@SuppressWarnings("unchecked")
		public Class<PT> type() {
			return (Class<PT>) this.field.getType();
		}

		@Override
		public boolean isReadable() {
			return true;
		}

		@Override
		public boolean isWritable() {
			return !Modifier.isFinal(this.field.getModifiers());
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof FieldProperty<?, ?>)) {
				return false;
			}
			FieldProperty<?, ?> other = (FieldProperty<?, ?>) obj;
			return Objects.equals(field, other.field);
		}

		@Override
		public int hashCode() {
			return Objects.hash(field);
		}
	}

	static final class ReadOnlyMethodProperty<CT, PT> extends Property<CT, PT> {
		private final Method getter;

		ReadOnlyMethodProperty(Method getter) {
			this.getter = requireNonNull(getter);
		}

		@SuppressWarnings({"unchecked", "PMD.AvoidThrowingRawExceptionTypes", "ThrowSpecificExceptions"})
		@Override
		@Nullable
		public PT get(CT bean) {
			try {
				return (PT) this.getter.invoke(bean);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			catch (InvocationTargetException e) {
				Throwables.throwIfUnchecked(e.getTargetException());
				throw new RuntimeException(e.getTargetException()); // SUPPRESS PreserveStackTrace
			}
		}

		@Override
		public void set(CT bean, @Nullable PT value) {
			throw new IllegalStateException("Property is not writable");
		}

		@Override
		public String name() {
			return propertyNameFromGetter(this.getter);
		}

		@Override
		public Class<PT> type() {
			@SuppressWarnings("unchecked")
			Class<PT> resultType = (Class<PT>) this.getter.getReturnType();
			return requireNonNull(resultType);
		}

		@Override
		public boolean isReadable() {
			return introspect(getter).isCallable();
		}

		@Override
		public boolean isWritable() {
			return false;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ReadOnlyMethodProperty<?, ?>)) {
				return false;
			}
			ReadOnlyMethodProperty<?, ?> other = (ReadOnlyMethodProperty<?, ?>) obj;
			return Objects.equals(getter, other.getter);
		}

		@Override
		public int hashCode() {
			return Objects.hash(getter);
		}
	}

	static final class WriteOnlyMethodProperty<CT, PT> extends Property<CT, PT> {
		private final Method setter;

		WriteOnlyMethodProperty(Method setter) {
			this.setter = requireNonNull(setter);
		}

		@Override
		@Nullable
		public PT get(CT bean) {
			throw new IllegalStateException("Property is not readable");
		}

		@SuppressWarnings({"unchecked", "PMD.AvoidThrowingRawExceptionTypes", "ThrowSpecificExceptions"})
		@Override
		public void set(CT bean, @Nullable PT value) {
			try {
				this.setter.invoke(bean, value);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			catch (InvocationTargetException e) {
				Throwables.throwIfUnchecked(e.getTargetException());
				throw new RuntimeException(e.getTargetException()); // SUPPRESS PreserveStackTrace
			}
		}

		@Override
		public String name() {
			return propertyNameFromSetter(this.setter);
		}

		@Override
		public Class<PT> type() {
			Class<?>[] parameterTypes = this.setter.getParameterTypes();
			@SuppressWarnings("unchecked") // checked at construction
				Class<PT> firstParameterType = (Class<PT>) parameterTypes[0];
			return firstParameterType;
		}

		@Override
		public boolean isReadable() {
			return false;
		}

		@Override
		public boolean isWritable() {
			return introspect(this.setter).isCallable();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof WriteOnlyMethodProperty<?, ?>)) {
				return false;
			}
			WriteOnlyMethodProperty<?, ?> other = (WriteOnlyMethodProperty<?, ?>) obj;
			return Objects.equals(setter, other.setter);
		}

		@Override
		public int hashCode() {
			return Objects.hash(setter);
		}
	}

	static final class ReadWriteMethodProperty<CT, PT> extends Property<CT, PT> {
		private final Method getter;
		private final Method setter;

		ReadWriteMethodProperty(Method getter, Method setter) {
			this.getter = getter;
			this.setter = setter;
		}

		@SuppressWarnings({"unchecked", "PMD.AvoidThrowingRawExceptionTypes", "ThrowSpecificExceptions"})
		@Override
		@Nullable
		public PT get(CT bean) {
			try {
				return (PT) this.getter.invoke(bean);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			catch (InvocationTargetException e) {
				Throwables.throwIfUnchecked(e.getTargetException());
				throw new RuntimeException(e.getTargetException()); // SUPPRESS PreserveStackTrace
			}
		}

		@Override
		@SuppressWarnings({"PMD.AvoidThrowingRawExceptionTypes", "ThrowSpecificExceptions"})
		public void set(CT bean, @Nullable PT value) {
			try {
				this.setter.invoke(bean, value);
			}
			catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			catch (InvocationTargetException e) {
				Throwables.throwIfUnchecked(e.getTargetException());
				throw new RuntimeException(e.getTargetException()); // SUPPRESS PreserveStackTrace
			}
		}

		@Override
		public String name() {
			return propertyNameFromGetter(this.getter);
		}

		@SuppressWarnings("unchecked")
		@Override
		public Class<PT> type() {
			return (Class<PT>) this.getter.getReturnType();
		}

		@Override
		public boolean isReadable() {
			return introspect(getter).isCallable();
		}

		@Override
		public boolean isWritable() {
			return introspect(setter).isCallable();
		}

		private static void checkCompatibility(Method getter, Method setter) {
			Class<?> getterReturnType = getter.getReturnType();
			Class<?> setterAcceptType = setter.getParameterTypes()[0];
			checkArgument(getterReturnType.equals(setterAcceptType));
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof ReadWriteMethodProperty<?, ?>)) {
				return false;
			}
			ReadWriteMethodProperty<?, ?> other = (ReadWriteMethodProperty<?, ?>) obj;
			return Objects.equals(getter, other.getter)
				&& Objects.equals(setter, other.setter);
		}

		@Override
		public int hashCode() {
			return Objects.hash(getter, setter);
		}
	}

	private Properties() {
		// utility
	}
}
