package org.perfectable.introspection.bean;

import org.perfectable.introspection.Introspection;
import org.perfectable.introspection.Methods;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.perfectable.introspection.bean.ReadOnlyMethodProperty.capitalizeWithPrefix;

final class WriteOnlyMethodProperty<CT, PT> implements Property<CT, PT> {
	private static final String SETTER_PREFIX = "set";

	private final Method setter;

	public static <CX, PX> WriteOnlyMethodProperty<CX, PX> forSetter(Method setter) {
		checkNotNull(setter);
		checkArgument(isSetter(setter));
		return new WriteOnlyMethodProperty<>(setter);
	}

	private WriteOnlyMethodProperty(Method setter) {
		this.setter = checkNotNull(setter);
	}

	// checked at construction
	@SuppressWarnings("unchecked")
	@Override
	@Nullable
	public PT get(CT bean) {
		throw new IllegalStateException("Property is not readable");
	}

	@Override
	public void set(CT bean, @Nullable PT value) {
		try {
			this.setter.invoke(bean, value);
		}
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new RuntimeException(e); // SUPPRESS no better exception here
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
		return checkNotNull(firstParameterType);
	}

	@Override
	public boolean isReadable() {
		return false;
	}

	@Override
	public boolean isWriteable() {
		return Methods.isCallable(this.setter);
	}

	static <CX, PX> Optional<Method> findSetter(Class<CX> beanClass, String name, Class<PX> type) {
		return Introspection.of(beanClass).methods()
				.named(setterName(name))
				.parameters(type)
				.returningVoid()
				.option();
	}

	private static boolean isSetter(Method method) {
		boolean doesntReturn = Void.TYPE.equals(method.getReturnType());
		boolean hasOneParameter = method.getParameterTypes().length == 1;
		boolean startsWithAppropriatePrefix = method.getName().startsWith(SETTER_PREFIX);
		return doesntReturn && hasOneParameter && startsWithAppropriatePrefix;
	}

	private static String propertyNameFromSetter(Method setter) {
		checkArgument(isSetter(setter));
		String unformatted = setter.getName();
		int prefixLength = SETTER_PREFIX.length();
		return String.valueOf(unformatted.charAt(prefixLength)).toLowerCase()
				+ unformatted.substring(prefixLength + 1);
	}

	public static String setterName(String name) {
		return capitalizeWithPrefix(SETTER_PREFIX, name);
	}

}
