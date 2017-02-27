package org.perfectable.introspection.bean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Optional;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.perfectable.introspection.Introspections.introspect;

final class ReadOnlyMethodProperty<CT, PT> implements Property<CT, PT> {
	private static final String BOOLEAN_GETTER_PREFIX = "is";
	private static final String STANDARD_GETTER_PREFIX = "get";
	private final Method getter;

	public static <CX, PX> ReadOnlyMethodProperty<CX, PX> forGetter(Method getter) {
		return new ReadOnlyMethodProperty<>(getter);
	}

	private ReadOnlyMethodProperty(Method getter) {
		this.getter = checkNotNull(getter);
	}

	// checked at construction
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public PT get(CT bean) {
		try {
			return (PT) this.getter.invoke(bean);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e); // SUPPRESS no better exception here
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
		return checkNotNull(resultType);
	}

	@Override
	public boolean isReadable() {
		return introspect(getter).isCallable();
	}

	@Override
	public boolean isWritable() {
		return false;
	}


	private static boolean isGetter(Method method) {
		boolean actuallyReturns = !Void.TYPE.equals(method.getReturnType());
		boolean hasNoParameters = method.getParameterTypes().length == 0;
		String appropriatePrefix = getterPrefix(method.getReturnType());
		boolean startsWithAppropriatePrefix = method.getName().startsWith(appropriatePrefix);
		return actuallyReturns && hasNoParameters && startsWithAppropriatePrefix;
	}

	private static String getterPrefix(Class<?> returnType) {
		boolean returnsBoolean = Boolean.class.equals(returnType)
				|| boolean.class.equals(returnType);
		return returnsBoolean ? BOOLEAN_GETTER_PREFIX : STANDARD_GETTER_PREFIX;
	}

	static String propertyNameFromGetter(Method getter) {
		checkArgument(isGetter(getter));
		String unformatted = getter.getName();
		int prefixLength = getterPrefix(getter.getReturnType()).length();
		return String.valueOf(unformatted.charAt(prefixLength)).toLowerCase(Locale.ROOT)
				+ unformatted.substring(prefixLength + 1);
	}

	private static String getterName(String name, Class<?> type) {
		String prefix = getterPrefix(type);
		return capitalizeWithPrefix(prefix, name);
	}

	static String capitalizeWithPrefix(String prefix, String name) {
		return prefix + name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
	}

	public static <CX, PX> Optional<Method> findGetter(Class<CX> beanClass, String name, Class<PX> type) {
		return introspect(beanClass).methods()
				.named(getterName(name, type))
				.parameters()
				.returning(type)
				.option();
	}
}
